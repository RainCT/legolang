TARGET = LegoLang

# Directories
ROOT =		$(PWD)
SRCDIR =	$(ROOT)/src
LIBDIR =	$(ROOT)/libs
CLASSDIR =	$(ROOT)/classes
MAIN =		$(SRCDIR)/cli
PARSER =	$(SRCDIR)/parser
INTERP =	$(SRCDIR)/interp
LLCCLIB =	$(SRCDIR)/llcclib
JAVADOC =	$(ROOT)/javadoc
BIN =		$(ROOT)/bin

# Executables
EXEC =		$(BIN)/llcc
JARFILE =	$(BIN)/$(TARGET).jar
MANIFEST =	$(BIN)/$(TARGET)_Manifest.txt

# Libraries and Classpath
LIB_ANTLR =		$(LIBDIR)/antlr3.jar
LIB_CODEMODEL =	$(LIBDIR)/codemodel-2.5~20110810.jar
LIB_IO =		$(LIBDIR)/commons-io-2.2.jar
LIB_CLI =		$(LIBDIR)/commons-cli-1.2.jar
CLASSPATH =		$(LIB_ANTLR):$(LIB_CODEMODEL):$(LIB_IO):$(LIB_CLI)
JARPATH =		"$(LIB_ANTLR) $(LIB_CODEMODEL) $(LIB_IO) $(LIB_CLI)"

# Distribution (tar) file
DATE =		$(shell date +"%d%b%y")
DISTRIB =	$(TARGET)_$(DATE).tgz

# Flags
JFLAGS =	-classpath $(CLASSPATH) -d $(CLASSDIR)
DOCFLAGS =	-classpath $(CLASSPATH) -d $(JAVADOC) -privat

# Source files
GRAMMAR =		$(PARSER)/$(TARGET).g

MAIN_SRC =		$(MAIN)/$(TARGET).java

PARSER_SRC =	$(PARSER)/$(TARGET)Lexer.java \
				$(PARSER)/$(TARGET)Parser.java \
				$(NULL)

INTERP_SRC =	$(INTERP)/Interp.java \
				$(INTERP)/CustomTree.java \
				$(INTERP)/CustomTreeAdaptor.java \
				$(INTERP)/Scope.java \
				$(INTERP)/Type.java \
				$(INTERP)/TypedExpression.java \
				$(INTERP)/ScopeStack.java \
				$(INTERP)/PortConfig.java \
				$(LLCCLIB)/DefaultValue.java \
				$(NULL)

ALL_SRC =		$(MAIN_SRC) $(PARSER_SRC) $(INTERP_SRC)

all: build exec # docs

build:
	antlr3 -o $(PARSER) $(GRAMMAR)
	if [ ! -e $(CLASSDIR) ]; then \
		mkdir $(CLASSDIR); \
	fi
	javac $(JFLAGS) $(ALL_SRC)

docs:
	javadoc $(DOCFLAGS) $(ALL_SRC)

exec:
	if [ ! -e $(BIN) ]; then\
		mkdir $(BIN);\
	fi
	echo "Main-Class: cli.LegoLang" > $(MANIFEST)
	echo "Class-Path: $(JARPATH)" >> $(MANIFEST)
	cd $(CLASSDIR); jar -cmf $(MANIFEST) $(JARFILE) *
	printf "#!/bin/sh\n\n" > $(EXEC)
	printf 'exec java -enableassertions -jar $(JARFILE) "$$@"' >> $(EXEC)
	chmod a+x $(EXEC)

clean:
	rm -rf $(BIN)/LegoLang.jar $(BIN)/LegoLang_Manifest.txt $(BIN)/llcc
	rm -rf $(CLASSDIR)
	rm -rf $(PARSER)/*.java $(PARSER)/*.tokens

check:
	./tests/run-tests.sh

distrib: clean
	rm -rf $(JAVADOC)
	rm -rf $(BIN)

tar: distrib
	cd ..; tar cvzf $(DISTRIB) $(TARGET); mv $(DISTRIB) $(TARGET); cd $(TARGET)
