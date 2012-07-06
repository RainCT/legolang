/**
 * Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
 * Copyright © 2012 Gerard Canal Camprodon <grar.knal@gmail.com>
 * Copyright © 2011 Jordi Cortadella
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package cli;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import java.io.*;
import java.util.*;

import parser.*;
import interp.*;

public class LegoLang {

    private String mInfile = null;
    private String mOutfile = null;
    private boolean mUpload = false;
    private boolean mSourceOnly = false;
    private boolean mDeleteSource = true;
    private boolean mDeleteBinary = false;
    private boolean mRestrictedMode = false;
    private boolean mDebugFile = false;
    private boolean mVerbose = false;
    private boolean mQuiet = false;

    public static void main(String[] args) throws Exception {
        LegoLang app = new LegoLang();
        if (app.parseOptions(args)) {
            System.exit(app.run());
        }
	}

    private LegoLang() {
    }

    private boolean parseOptions(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("h", "help", false, "show this message");
        options.addOption("o", "output-file", true, "output file");
        options.addOption("v", "verbose", false, "show debugging information");
        options.addOption("q", "quiet", false, "don't show any output on success");
        options.addOption("u", "upload", false, "upload program");
        options.addOption("U", "upload-only", false, "upload program (doesn't generate .nxj)");
        options.addOption("C", "code", false, "don't delete the generated code");
        options.addOption("S", "source-only", false, "only generate code (don't build)");
        options.addOption("d", "debug", false, "create debugging file (.nxd)");
        options.addOption("R", "restricted", false,
            "build into a standard .jar (for testing purposes)");
            //"build with javac (no Lego functions supported)");

        CommandLineParser optionParser = new GnuParser();
        boolean showHelp = false;

        CommandLine line = null;
        try {
            line = optionParser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            showHelp = true;
        } catch (MissingArgumentException e) {
            System.err.println(e.getMessage());
            return false;
        }

        final String usage = "llcc [options] <file>";

        if (showHelp || line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(usage, options);
            return false;
        }

        if (line.hasOption("output-file")) {
            mOutfile = line.getOptionValue("output-file");
        }

        if (line.hasOption("verbose")) {
            mVerbose = true;
        }

        if (line.hasOption("quiet")) {
            mQuiet = true;
        }

        if (line.hasOption("upload")) {
            mUpload = true;
        }

        if (line.hasOption("upload-only")) {
            mUpload = true;
            mDeleteBinary = true;
            if (mOutfile != null) {
                System.err.println(
                    "Option --output-file can't be used with --upload-only.");
                return false;
            }
        }

        if (line.hasOption("code")) {
            mDeleteSource = false;
        }

        if (line.hasOption("source-only")) {
            mSourceOnly = true;
            mDeleteSource = false; // implied
            if (mUpload) {
                System.err.println(
                    "Option --source-only can't be used with --upload.");
                return false;
            }
            if (mOutfile != null) {
                System.err.println("Warning: Option --output-file discarded.");
            }
        }

        if (line.hasOption("restricted")) {
            mRestrictedMode = true;
            if (mUpload) {
                System.err.println(
                    "Option --restricted can't be used with --upload.");
                return false;
            }
        }

        if (line.hasOption("debug")) {
            mDebugFile = true;
            if (mSourceOnly || mDeleteBinary || mRestrictedMode) {
                System.err.println(
                    "Some other options are incompatible with --debug.");
                return false;
            }
        }

        String[] files = line.getArgs();
        if (files.length != 1) {
            System.err.println("Invalid command line.\n");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(usage, options);
            return false;
        }

        mInfile = files[0];
        return true;
    }

	public int run() throws Exception {
		CharStream input = new ANTLRFileStream(mInfile);

		LegoLangLexer lex = new LegoLangLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);

		LegoLangParser parser = new LegoLangParser(tokens);

		LegoLangParser.prog_return result = null;
		CustomTreeAdaptor adaptor = new CustomTreeAdaptor();
		parser.setTreeAdaptor(adaptor);
        try {
            result = parser.prog();
        } catch (Exception e) {
            System.err.println("Unknown error: " + e);
            return 2;
        }

        int nerrors = lex.getNumberOfSyntaxErrors();
        nerrors += parser.getNumberOfSyntaxErrors();
        if (nerrors > 0) {
            System.err.println(nerrors + " errors detected.");
            return 2;
        }

		CustomTree tree = (CustomTree) result.getTree();

        File resultDir = new File("./llcc-build-area/");

        // Clean up build directory, deleting any output from
        // previous executions
        try {
            FileUtils.cleanDirectory(resultDir);
        } catch (IllegalArgumentException e) {
            // The directory doesn't exist :)
        }

        // Write AST tree to file
        resultDir.mkdirs();
		File ast = new File("llcc-build-area/parse_tree.ast");
		BufferedWriter output = new BufferedWriter(new FileWriter(ast));
		output.write(tree.toStringTree());
		output.close();

        // Generate the Java code
		Interp interpreter = new Interp(tree);
        try {
            PrintStream codemodelStream = new PrintStream(new NullOutputStream());
            //codemodelStream = System.out;
    		interpreter.compile(resultDir, codemodelStream);
            if (!mQuiet)
                System.err.println("Code written.");
        } catch (RuntimeException e) {
            if (mVerbose) {
                e.printStackTrace();
            } else {
                System.err.println(e.getMessage());
            }
            return 1;
        }

        // Stop here if --source-only was used
        if (mSourceOnly)
            return 0;

        ProcessBuilder pb;

        // Compile the Java code
        String compiler = /*(mRestrictedMode) ? "javac" :*/ "nxjc";
        pb = new ProcessBuilder(compiler, "Main.java");
        pb.directory(resultDir);
        if (!runProcess(pb, "Code compiled.", "COMPILATION FAILED!"))
            return 3;

        // Determine output filename
        String outputFile, outputFilePath;
        if (mOutfile != null) {
            outputFile = mOutfile;
            outputFilePath = (outputFile.startsWith("/")) ? outputFile : "../" + outputFile;
        } else {
            outputFile = new File(mInfile).getName();
            String ext = (mRestrictedMode) ? ".jar" : ".nxj";
            try {
                outputFile = outputFile.substring(0, outputFile.lastIndexOf(".")) + ext;
            } catch (StringIndexOutOfBoundsException e) {
                outputFile = outputFile + ext;
            }
            outputFilePath = "../" + outputFile;
        }

        // Determine debug file name (if needed)
        String debugFilePath = null;
        if (mDebugFile) {
            String base = new File(outputFilePath).getParent();
            if (base == null) base = "..";
            debugFilePath = base + File.separator + outputFile.substring(
                0, outputFile.lastIndexOf(".")) + ".nxd";
        }

        if (!mRestrictedMode) {
            // Link the Java code
            List<String> command = new LinkedList<String>();
            command.addAll(Arrays.asList("nxjlink", "-o", outputFilePath, "Main"));
            if (mDebugFile) {
                command.add("-od");
                command.add(debugFilePath);
            }
            pb = new ProcessBuilder(command);
            pb.directory(resultDir);
            String successMsg = "Program linked into " + outputFile + ".";
            if (mDebugFile)
                successMsg += " Created debug file " +
                    new File(debugFilePath).getName() + ".";
            if (!runProcess(pb, successMsg, "PROGRAM LINKING FAILED!"))
                return 3;
        } else {
            // Create an executable .jar file
            FileUtils.writeStringToFile(
                new File("llcc-build-area/manifest.txt"),
                "Main-Class: Main\n");
            List<String> command = new LinkedList<String>();
            command.addAll(Arrays.asList("jar", "cfm", outputFilePath,
                "manifest.txt"));
            File[] classFiles = new File("llcc-build-area/").listFiles(
                (FilenameFilter) new WildcardFileFilter("*.class"));
            for (int i = 0; i < classFiles.length; ++i) {
                command.add(classFiles[i].getName());
            }
            classFiles = new File("llcc-build-area/llcclib/").listFiles(
                (FilenameFilter) new WildcardFileFilter("*.class"));
            for (int i = 0; i < classFiles.length; ++i) {
                command.add("llcclib/" + classFiles[i].getName());
            }
            pb = new ProcessBuilder(command);
            pb.directory(resultDir);
            String successMsg = "Program built into " + outputFile + ".";
            if (!runProcess(pb, successMsg, "JAR CREATION FAILED!"))
                return 3;
        }

        if (mDeleteSource) {
            FileUtils.deleteQuietly(resultDir);
        }

        if (mUpload) {
            pb = new ProcessBuilder("nxjupload", outputFile);
            if (!runProcess(pb, "Program uploaded!", "UPLOAD FAILED!"))
                return 4;
            if (mDeleteBinary) {
                FileUtils.deleteQuietly(new File(outputFile));
            }
        }

        return 0;
	}

    private boolean runProcess(ProcessBuilder pb, String successMessage,
            String errorMessage) throws Exception {

        Process process;

        try {
            process = pb.start();
        } catch (IOException e) {
            System.err.println("\n" + errorMessage);
            System.err.println(e.getLocalizedMessage());
            return false;
        }

        process.waitFor();
        if (process.exitValue() != 0) {
            System.err.println("\n" + errorMessage);
            showProcessOutput(process);
            return false;
        } else {
            if (!mQuiet)
                System.err.println(successMessage);
            return true;
        }
    }

    private void showProcessOutput(Process process) throws IOException {
        System.err.println("------------------------------------------");
        InputStream err = process.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(err));
        String line;
        while ((line = reader.readLine()) != null) {
            System.err.println(line);
        }
        System.err.println("------------------------------------------");
    }

}
