// Array definition
int[] as
as[] = 5 // a[0]
as[] = 6 // a[1]
as[] = 7 // a[2]
as[] = 8 // a[3]

// Array indexing
int i = 2
print as[i] + 1
print ""

// Array copy
int[] bs = as
bs[3] = 2
print as[0] + " " + as[1] + " " + as[2] + " " + as[3] + "!"
print bs[0] + " " + bs[1] + " " + bs[2] + " " + bs[3] + "!"
print ""

// Displaying an array
print bs
print ""

// Array size
print "Array size: " + #bs + " (bs), " + #as + " (as)!"
int[] empty_array
print "An empty array has size " + #empty_array

// Appending to an array
empty_array[] = 42
print "After adding one element to it, it's size is " + #empty_array
print ""

// Negative array indexing
as[-2] = 3
print "8 == as[-1] == " + as[-1]
print "3 == as[-2] == " + as[-2]
print "6 == as[-3] == " + as[-3]
print "5 == as[-4] == " + as[-4]
print ""

// Arrays of floats
float[] foo
foo[] = 5.4
foo[] = 3
foo[] = i // check cast from int
print foo
print ""

// Array copy
float[] x = foo
print x
print ""

// Arrays of strings
string[] txts
txts[] = "hello!"
txts[] = "you!"
txts[] = "how are you?"
txts[] = txts[0] + " " + txts[-1]
print txts
print ""

// Indexing with bools
string[] two
two[] = "this is false"
two[] = "this is true"
print two[true]
