def fibo(int n) returns int:
    if n == 0: return 0
    if n == 1 or n == 2: return 1
    return fibo(n-1) + fibo(n-2)

for i in 1 .. 10:
    print fibo(i)
