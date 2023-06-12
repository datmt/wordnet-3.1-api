package io.ukata.wn.services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
public class DictionaryService {

    private final Dictionary dictionary;

    public DictionaryService() throws Exception {
        this.dictionary = Dictionary.getDefaultResourceInstance();
    }


    public Dictionary getDictionary() {
        return dictionary;
    }

    public Map<String, ArrayNode> getDefinition(String lemma) throws JWNLException {
        //Shamelessly copy from https://github.com/jacopofar/wordnet-as-a-service/blob/master/src/main/java/com/github/jacopofar/wordnetservice/Server.java
        HashSet<String> seenGlosses = new HashSet<>();
        Map<String, ArrayNode> posMap = new HashMap<>();
        ArrayNode definitionArray = JsonNodeFactory.instance.arrayNode();
        for (IndexWord w : dictionary.lookupAllIndexWords(lemma).getIndexWordArray()) {
            for (Synset sense : w.getSenses()) {
                if (seenGlosses.contains(sense.getGloss()))
                    continue;
                ObjectNode syn = JsonNodeFactory.instance.objectNode();
                var glossaryAndDefinition = this.glossaryToExamples(sense.getGloss());
                seenGlosses.add(sense.getGloss());
                syn.put("POS", w.getPOS().getLabel());
                syn.put("glossary", glossaryAndDefinition[0]);
                if (!glossaryAndDefinition[1].isEmpty())
                    syn.put("examples", glossaryAndDefinition[1]);
                syn.put("related_terms", Arrays.deepToString(sense.getWords().stream().map(Word::getLemma).distinct().toArray()));

                var arrayNode = posMap.get(w.getPOS().getLabel());
                if (arrayNode == null) {
                    arrayNode = JsonNodeFactory.instance.arrayNode();
                    posMap.put(w.getPOS().getLabel(), arrayNode);
                }

                arrayNode.add(syn);
            }
        }

        return posMap;
    }

    private String[] glossaryToExamples(String glossary) {
        //the example starts at the first quote ", before that it's the definition
        if (glossary.isEmpty())
            return new String[]{"", ""};


        var definition = glossary.split("\"")[0].trim();

        definition = definition.trim();
        //remove the trailing ;
        if (definition.endsWith(";"))
            definition = definition.substring(0, definition.length() - 1);


        if (definition.length() == glossary.length())
            return new String[]{definition, ""};

        var examples = glossary.substring(definition.length() + 1);
        return new String[]{definition, Arrays.stream(examples.split(";")).map(ex -> {
                    //remove the quote and the beginning and the end
                    ex = ex.trim();
                    if (ex.startsWith("\""))
                        ex = ex.substring(1);
                    if (ex.endsWith("\""))
                        ex = ex.substring(0, ex.length() - 1);
                    return ex;

                })
                .toList().toString()};
    }
}
