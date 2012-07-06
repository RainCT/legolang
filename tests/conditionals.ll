print "hello"

if true:
	print "we are going to test some basic language features..."
else:
	print "I don't feel like testing today"

if false:
	print "starting bad"
else:
	print "okay..."

if 1 == 1:
	print "are you ready for this?"
else if 1 == 2:
	print "I'm not into maths"
else:
	print "not really..."

if "a" == "b":
	print "the world is gonna explode"
else if "still not" == "right":
	print "NoNoNoNoNo!"
else if "hello world" == "hello world":
	print "Ökay, let's go! òèàáéó"

if "don't" == "print":
	print "failed"

print "I really like if's"

if true:
	pass
// no else this time :)

if true:
	print "tautology"
else if true:
	print "failed x2"

if false:
	print "false"
else if true:
	print "obviously"

if 0:
	print "false"
else if not 1:
	print "failed: not 1 == false"
else if 1:
	print "wait for it..."
	print "legen"

if not 42:
	print "the answer"
else if not 21:
	print "half the answer"
else:
	print "Looking good"

if (1 + 2):
	print 3

if -5:
	print "any number other than zero is true"

int num = 4
if -num:
	print "for variables..."

if not num:
	print "no!"

if 0.0:
	print "o_O"
else if -5.4:
	print "and even for floats"

print "---"

def tautology() returns bool:
	return true

def answer() returns int:
	return 42

if tautology():
	print "function call"
	if not tautology():
		print "why?"
	else:
		print "nested if"
		if answer() > 0:
			print "awesome"

print "success"

// vim: noexpandtab ts=4 sw=4
