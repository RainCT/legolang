// Array of ints

def sum(int[] a) returns int:
    int result = 0
    int i = 0
    while i < #a:
        result += a[i]
        a[i] = 0 // test pass by value
        i↑
    return result

int[] numbers // FIXME: = [1, 2, 3, 20, 5, 4, 19]
numbers[] = 1
numbers[] = 2
numbers[] = 3
numbers[] = 20
numbers[] = 5
numbers[] = 4
numbers[] = 19
print numbers
print sum(numbers)
print numbers
print ""

// Array of strings

def mutate(string[] a):
    a[0] = "blargh"

string[] s
s[] = "hello!"

print s
mutate(s)
print s
