/**
 * Copyright 2013 Jun Ohtani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.johtani.lucene.kuromoji;

import org.apache.commons.cli.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.GraphvizFormatter;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.dict.ConnectionCosts;
import org.apache.lucene.analysis.ja.dict.UserDictionary;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.IOUtils;

import java.io.*;
import java.util.Arrays;

public class GraphVizOutput {


    private Options options;
    private String text;
    private String userDictPath;
    private boolean discardPunctuation = true;
    private boolean discardCompoundToken = true;
    private JapaneseTokenizer.Mode mode = JapaneseTokenizer.Mode.SEARCH;

    private static String DISCARD_PUNCTUATION = "discard_punctuation";
    private static String MODE = "mode";
    private static String HELP = "help";
    private static String DISCARD_COMPOUND_TOKEN = "discard_compound_token";
    private static String USER_DICT = "userdict";

    private GraphVizOutput() {
        options = new Options();
        options.addOption(
                new Option("h", HELP, false, "print this message")
        );
        options.addOption(
                new Option(USER_DICT, true, "User Dictionary Path")
        );
        options.addOption(
                new Option("m", MODE, true, "Tokenization mode: NORMAL or SEARCH or EXTENDED. Default is SEARCH")
        );
        options.addOption(
                new Option("dp", DISCARD_PUNCTUATION, true, "true if punctuation tokens should be dropped from the output. Default is true.")
        );
        options.addOption(
                new Option("dct", DISCARD_COMPOUND_TOKEN, true, "true if compound tokens should be dropped from the output when tokenization mode is not NORMAL. Default is true.")
        );
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar KuromojiGraphVizOutputTool.jar [OPTIONS]... TEXT", options);
        System.exit(0);
    }

    private void parseArgs(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(HELP)) {
                printHelp();
            }
            if (line.hasOption(MODE)) {
                mode = JapaneseTokenizer.Mode.valueOf(line.getOptionValue(MODE, "SEARCH"));
            }
            if (line.hasOption(DISCARD_PUNCTUATION)) {
                discardPunctuation = Boolean.valueOf(line.getOptionValue(DISCARD_PUNCTUATION, "true"));
            }
            if (line.hasOption(DISCARD_COMPOUND_TOKEN)) {
                discardCompoundToken = Boolean.valueOf(line.getOptionValue(DISCARD_COMPOUND_TOKEN, "true"));
            }
            if (line.hasOption(USER_DICT)) {
                userDictPath = line.getOptionValue(USER_DICT);
            }

            String[] commandArgs = line.getArgs();
            if (commandArgs.length == 0) {
                System.err.println("\"TEXT\" is required. Please add text what you want to tokenize.");
                System.err.println("");
                printHelp();
            }
            text = String.join(" ", commandArgs);

        } catch (ParseException e) {
            System.err.println("Unexpected arguments: " + e.getMessage());
            printHelp();
        }

    }

    private void tokenize() throws Exception{
        final GraphvizFormatter gv2 = new GraphvizFormatter(ConnectionCosts.getInstance());
        final Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                JapaneseTokenizer tokenizer = new JapaneseTokenizer(readDict(userDictPath), discardPunctuation,
                        discardCompoundToken, mode);
                tokenizer.setGraphvizFormatter(gv2);
                return new TokenStreamComponents(tokenizer, tokenizer);
            }
        };


        TokenStream ts = analyzer.tokenStream("ignored", new StringReader(this.text));
        CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
        ts.reset();
        while(ts.incrementToken()){
            System.err.println(cta.toString());
        }
        System.out.println("#### dot file ####");
        String graphviz = gv2.finish();
        System.out.println(graphviz);
    }


    public static void main(String[] args)throws Exception{
        GraphVizOutput command = new GraphVizOutput();
        command.parseArgs(args);
        command.tokenize();
    }


    public static UserDictionary readDict(String path) {
        if(path == null || path.isEmpty()) return null;
        try {
            FileInputStream fis = new FileInputStream(path);

            try {
                Reader reader = new InputStreamReader(fis, IOUtils.UTF_8);
                return UserDictionary.open(reader);
            } finally {
                fis.close();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
