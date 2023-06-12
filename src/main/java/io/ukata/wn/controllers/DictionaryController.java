package io.ukata.wn.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.ukata.wn.services.DictionaryService;
import lombok.RequiredArgsConstructor;
import net.sf.extjwnl.JWNLException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/definition/{word}")
    public Map<String, ArrayNode> getDefinition(@PathVariable String word) throws JWNLException {
        return dictionaryService.getDefinition(word);
    }
}
