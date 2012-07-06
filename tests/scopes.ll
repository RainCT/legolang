int i = 1

def foo(int a=0):
    int i = 2
    print i
    if 1:
        int j = 3
        print j
    if 0:
        pass
    else:
        int j = 4
        print j
    print i

print i
foo()
print i
