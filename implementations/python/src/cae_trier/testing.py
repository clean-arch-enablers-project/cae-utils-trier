from cae_trier import ExhaustionReason, trier_of
from cae_mapped_exceptions import InternalMappedException


def letsSee(param: str) -> None:
    print(f"running...{param}")
    raise InternalMappedException.with_full_details("ouch", "oh yeah")


def handle_exhaustion(exhaustion: ExhaustionReason):
    raise Exception("ouch cansei...")


def handle_unexpected(unexpected: Exception):
    raise Exception("ouch...")


def just_print_2(param: str):
    print("2" + param)


trier_of(letsSee)\
    .retry_on(Exception, amount=3, base_time=0.5)\
    .retry_on(ExceptionGroup, amount=3, base_time=2)\
    .on_exhaustion_do(handle_exhaustion)\
    .on_unexpected_exceptions_do(handle_unexpected)\
    .set_fallback(just_print_2)\
    .execute("hhhhhhhhhhhhhhhhey")
