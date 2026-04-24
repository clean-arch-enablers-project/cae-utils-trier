from __future__ import annotations
import time
from typing import Callable, Generic, TypeVar, ParamSpec
from cae_mapped_exceptions import MappedException, InternalMappedException

INPUT = ParamSpec("INPUT")
OUTPUT = TypeVar("OUTPUT")


class ExhaustionReason:
    def __init__(self, exception: Exception, retry_policy: RetryPolicy):
        self.exception = exception
        self.retry_policy = retry_policy


class RetryPolicy:
    def __init__(self, max_retries: int, base_time: float):
        self.__max_retries = max_retries
        self.__base_time = base_time
        self.current_retries = 0

    def count_retry_on(self, problem: Exception) -> float:
        if (self.current_retries >= self.__max_retries):
            raise NoRetriesLeftMappedException(problem, self)
        time_to_wait = ((self.__base_time) * (2 ** self.current_retries))
        self.current_retries += 1
        return time_to_wait


class NoRetriesLeftMappedException(InternalMappedException):
    def __init__(self, problem: Exception, exhausted_policy: RetryPolicy):
        super().__init__(
            "No retries left: retried " +
            f"{exhausted_policy.current_retries}x " +
            f"on {problem.__class__.__name__}"
        )
        self.reason = ExhaustionReason(problem, exhausted_policy)


class Trier(Generic[INPUT, OUTPUT]):
    def __init__(
            self,
            action: Action[INPUT, OUTPUT],
            exha_handler: Callable[[ExhaustionReason], MappedException] | None,
            unexpe_exceptions_handler: Callable[[Exception], MappedException]
    ):
        self._action = action
        self.__exhaustion_handler = exha_handler
        self.__fallback = None
        self.__unexpected_exceptions_handler = unexpe_exceptions_handler

    def set_fallback(
            self,
            action: Callable[INPUT, OUTPUT]
    ) -> Trier[INPUT, OUTPUT]:
        self.__fallback = action
        return self

    def execute(self, *args: INPUT.args, **kwargs: INPUT.kwargs) -> OUTPUT:
        try:
            return self._action.execute(*args, **kwargs)
        except NoRetriesLeftMappedException as exhaustion:
            if self.__exhaustion_handler is not None:
                if self.__fallback is not None:
                    return self.__run_fallback(
                        self.__fallback,
                        exhaustion,
                        *args,
                        **kwargs
                    )
                else:
                    raise self.__exhaustion_handler(exhaustion.reason)
            elif self.__fallback is not None:
                return self.__run_fallback(
                    self.__fallback,
                    exhaustion,
                    *args,
                    **kwargs
                )
            else:
                raise exhaustion
        except MappedException as mapped_exception:
            if self.__fallback is not None:
                return self.__run_fallback(
                    self.__fallback,
                    mapped_exception,
                    *args,
                    **kwargs
                )
            else:
                raise mapped_exception
        except Exception as problem:
            if self.__fallback is not None:
                return self.__run_fallback(
                    self.__fallback,
                    problem,
                    *args,
                    **kwargs
                )
            else:
                raise self.__unexpected_exceptions_handler(problem)

    def __run_fallback(
            self,
            task: Callable[INPUT, OUTPUT],
            original_problem: Exception,
            *args: INPUT.args,
            **kwargs: INPUT.kwargs
    ) -> OUTPUT:
        try:
            return task(*args, **kwargs)
        except Exception as fallback_error:
            raise FallbackFailureMappedException(
                fallback_error,
                original_problem
            )


class FallbackFailureMappedException(MappedException):
    def __init__(self, failure: Exception, original: Exception):
        super().__init__(
            f"Fallback action failed. {failure.__class__.__name__}: " +
            f"{failure.__str__()}",
            "Before fallback the error was " +
            f"{original.__class__.__name__}: {original.__str__()}"
        )


class Action(Generic[INPUT, OUTPUT]):
    def __init__(
            self,
            callable: Callable[INPUT, OUTPUT],
            retry_policies: dict[type[Exception], RetryPolicy]
    ):
        self.__callable = callable
        self.__retry_policies = retry_policies

    def execute(self, *args: INPUT.args, **kwargs: INPUT.kwargs) -> OUTPUT:
        try:
            return self.__callable(*args, **kwargs)
        except Exception as problem:
            policy = self.__retry_policies.get(type(problem))
            if policy is not None:
                delay_to_retry = policy.count_retry_on(problem)
                time.sleep(delay_to_retry)
                return self.execute(*args, **kwargs)
            else:
                raise problem


class Builder(Generic[INPUT, OUTPUT]):

    def __init__(self, action: Callable[INPUT, OUTPUT]):
        self.__action = action
        self.__retry_policies: dict[type[Exception], RetryPolicy] = {}
        self.__exha_handler: Callable[
            [ExhaustionReason],
            MappedException
        ] | None = None
        self.__unexp_excep_handler: Callable[
            [Exception],
            MappedException
        ] | None = None

    def retry_on(
            self,
            exception: type[Exception],
            amount: int,
            base_time: float
    ) -> Builder[INPUT, OUTPUT]:
        self.__retry_policies[exception] = RetryPolicy(amount, base_time)
        return self

    def on_exhaustion_do(
            self,
            handler: Callable[[ExhaustionReason], MappedException]
    ) -> Builder[INPUT, OUTPUT]:
        self.__exha_handler = handler
        return self

    def on_unexpected_exceptions_do(
            self,
            handler: Callable[[Exception], MappedException]
    ) -> Trier[INPUT, OUTPUT]:
        self.__unexp_excep_handler = handler
        return Trier(
            Action(self.__action, self.__retry_policies),
            self.__exha_handler,
            self.__unexp_excep_handler
        )


def trier_of(action: Callable[INPUT, OUTPUT]) -> Builder[INPUT, OUTPUT]:
    return Builder(action)
