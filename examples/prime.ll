def is_prime(int n) returns bool:
    if n == 1: return false
    int div = 2
    while div*div <= n:
        if n % div == 0: return false
        ↑div
    return true

for i in 1 .. 1000:
    if is_prime(i):
        print i + " is a prime number."
