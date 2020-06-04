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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.GraphvizFormatter;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.dict.ConnectionCosts;
import org.apache.lucene.analysis.ja.dict.UserDictionary;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.IOUtils;

import java.io.*;

public class GraphVizOutput {

    public static void main(String[] args)throws Exception{

        if(args.length == 0){
            System.err.println("Usage: java -jar KuromojiGraphVizOutputTool.jar <input string> [user_dictionary_path]");
            System.exit(0);
        }

        String input = args[0];
        final String userDictPath = args.length < 2 ? null : args[1];

        final GraphvizFormatter gv2 = new GraphvizFormatter(ConnectionCosts.getInstance());
        final Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                JapaneseTokenizer tokenizer = new JapaneseTokenizer(readDict(userDictPath), true, false, JapaneseTokenizer.Mode.SEARCH);
                tokenizer.setGraphvizFormatter(gv2);
                return new TokenStreamComponents(tokenizer, tokenizer);
            }
        };


        TokenStream ts = analyzer.tokenStream("ignored", new StringReader(input));
        CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
        ts.reset();
        while(ts.incrementToken()){
            System.err.println(cta.toString());
        }
        System.out.println("#### dot file ####");
        String graphviz = gv2.finish();
        System.out.println(graphviz);
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
