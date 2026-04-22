from trier import ExhaustionReason, trier_of

def letsSee(param: str):
    print("iiiiiiiiiiha")

def handle_exhaustion(exhaustion: ExhaustionReason):
    raise Exception("ouch...")

def handle_unexpected(unexpected: Exception):
    raise Exception("ouch...")

def just_print_2(param: str):
    print("2")

trier_of(letsSee)\
    .retry_on(Exception, amount=3, base_time=0.5)\
    .retry_on(ExceptionGroup, amount=5, base_time=1)\
    .on_exhaustion_do(handle_exhaustion)\
    .on_unexpected_exceptions_do(handle_unexpected)\
    .set_fallback(just_print_2)\
    .execute("")