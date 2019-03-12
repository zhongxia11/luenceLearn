package com.lucene;

import com.google.common.collect.Maps;
import nearRealTime.Searcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class RestController {


    @RequestMapping(value = "/add", method = RequestMethod.POST,headers = "Accept=application/json")
    public Object add(HttpServletResponse response,@RequestBody Doc doc) {
        Map<String,Short> map = Maps.newHashMap();
        boolean result = Searcher.addIndex(doc);
        map.put("status",result?(short)0:(short)1);
        return map;
    }

    @GetMapping("/query/{value}")
    @ResponseBody
    public Object query(@PathVariable("value") String value, HttpServletResponse response) {
        Map<String,String> result = Maps.newHashMap();
        try {
            Map<String,String> resultTmp = Searcher.search(value);
            result.put("size",resultTmp.size()+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }



}
