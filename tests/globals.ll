global int t
global int g = 512
global string s = "Hello"
global int[] e
global float flo = 0.25

t = 27

def foo():
    t = 28

print s + " " + g + " " + t + " " + flo
foo()
print s + " " + g + " " + t + " " + flo
