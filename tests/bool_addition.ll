bool a = false
print a             // false

int b = a + a
print b             // 0

a = true
b = a + a
print b             // 2

b = 2 + a
print 3             // 3

b = 3
print b + a         // 4

/* Check some int to bool casting while we are at it */
print ""

bool b0 = b + a
print b0            // true

int z = 0
bool b1 = z
print b1            // false
