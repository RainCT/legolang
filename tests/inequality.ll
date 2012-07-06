// INT
int i1 = 5
int i2 = 5
int i3 = 6
print 1 != 2
print 5 != i1
print i1 != i2
print i2 != i3
print ""

// FLOAT
float f1 = 42.0
print 50 != f1
print f1 != 42.0
print ""

// BOOL
print true != true
print false != false
print true != false
print false != true
print ""

// STRING
string s3 = "a"
string s4 = "hello world!"
string s5 = "hello world!"
print "hi" != "bye"
print "hi" != "hi"
print s3 != s4
print s4 != s5
print (s4 + s3) != ("hello world!" + s3)
print ""

// ARRAY (int)
int[] a
int[] b
int[] c
a[] = 1
a[] = 2
b[] = 1
b[] = 2
c[] = 1
c[] = 3
print a[0] != a[0]
print a[0] != b[0]
print a[1] != b[1]
print a[1] != c[1]
print a != a
print a != b
print a != c
print ""

// COLOR
//print RED != RED
//print GREEN != BLUE

// FUNCTIONS

def fX(int x) returns int:
    return x
print fX(1) != fX(2)
print fX(4) != fX(4)
