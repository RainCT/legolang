def func(int x=0):
    print x

func(5)
func()

print ""

def mixed(int a, int b, int c=5):
    print a+b+c

mixed(1, 2, 3)
mixed(1, 2)
