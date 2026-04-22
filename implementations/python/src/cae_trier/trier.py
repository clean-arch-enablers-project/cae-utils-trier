from __future__ import annotations
import time
from typing import Callable, Generic, TypeVar, ParamSpec

I = ParamSpec("I")
O = TypeVar("O")

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

class NoRetriesLeftMappedException(Exception):
    def __init__(self, problem: Exception, exhausted_policy: RetryPolicy):
        super().__init__(f"No retries leff: retried up to {exhausted_policy.current_retries} on {problem.__class__.__name__}")
        self.reason = ExhaustionReason(problem, exhausted_policy)

class Trier(Generic[I, O]):
    def __init__(
            self, 
            action: Action[I, O], 
            on_exhaustion_handler: Callable[[ExhaustionReason], Exception] | None,
            on_unexpected_exceptions_handler: Callable[[Exception], Exception]):
        self._action = action
        self.__on_exhaustion_handler = on_exhaustion_handler
        self.__fallback = None
        self.__on_unexpected_exceptions_handler = on_unexpected_exceptions_handler

    def set_fallback(self, action: Callable[I, O]) -> Trier[I, O]:
        self.__fallback = action
        return self

    def execute(self, *args: I.args, **kwargs: I.kwargs) -> O:
        try:
            return self._action.execute(*args, **kwargs)
        except NoRetriesLeftMappedException as exhaustion:
            if self.__on_exhaustion_handler is not None:
                handling = self.__on_exhaustion_handler(exhaustion.reason)
                if self.__fallback is not None:
                    return self.__run_fallback(self.__fallback, handling, *args, **kwargs)
                else:
                    raise handling
            elif self.__fallback is not None:
                return self.__run_fallback(self.__fallback, exhaustion, *args, **kwargs)
            else:
                raise exhaustion
        # except MappedException as mapped_exception:
        #     raise mapped_exception
        except Exception as problem:
            handling = self.__on_unexpected_exceptions_handler(problem)
            if self.__fallback is not None:
                return self.__run_fallback(self.__fallback, handling, *args, **kwargs)
            else:
                raise handling
        
    def __run_fallback(self, task: Callable[I, O], original_problem: Exception, *args: I.args, **kwargs: I.kwargs) -> O:
        try:
            return task(*args, **kwargs)
        except Exception as fallback_error:
            raise FallbackFailure(fallback_error, original_problem)

class FallbackFailure(Exception):
    def __init__(self, failure: Exception, original: Exception):
        super().__init__(f"Fallback action failed. {failure.__class__.__name__}: {failure.__str__()} | Before fallback the error was {original.__class__.__name__}: {original.__str__()}")

class Action(Generic[I, O]):
    def __init__(self, callable: Callable[I, O], retry_policies: dict[type[Exception], RetryPolicy]):
        self.__callable = callable
        self.__retry_policies = retry_policies

    def execute(self, *args: I.args, **kwargs: I.kwargs) -> O:
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

class Builder(Generic[I, O]):
    
    def __init__(self, action: Callable[I, O]):
        self.__action = action
        self.__retry_policies: dict[type[Exception], RetryPolicy] = {}
        self.__exhaustion_handler: Callable[[ExhaustionReason], Exception] | None = None
        self.__unexpected_exceptions_handler: Callable[[Exception], Exception] | None = None
    
    def retry_on(self, exception: type[Exception], amount: int, base_time: float) -> Builder[I, O]:
        self.__retry_policies[exception] = RetryPolicy(amount, base_time)
        return self
    
    def on_exhaustion_do(self, handler: Callable[[ExhaustionReason], Exception]) -> Builder[I, O]:
        self.__exhaustion_handler = handler
        return self
    
    def on_unexpected_exceptions_do(self, handler: Callable[[Exception], Exception]) -> Trier[I, O]:
        self.__unexpected_exceptions_handler = handler
        return Trier(
            Action(self.__action, self.__retry_policies), 
            self.__exhaustion_handler,
            self.__unexpected_exceptions_handler
        )


def trier_of(action: Callable[I, O]) -> Builder[I, O]:
    return Builder(action)
