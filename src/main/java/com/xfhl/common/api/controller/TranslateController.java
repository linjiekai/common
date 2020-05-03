package com.xfhl.common.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.xfhl.common.api.service.ITranslateService;
import com.xfhl.common.api.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translate")
public class TranslateController {

    @Autowired
    ITranslateService iTranslateService;

    @PostMapping("/simpleToTraditional")
    public Object simpleToTraditional(@RequestBody JSONObject jsonObject) throws Exception {
        String str = jsonObject.getString("str");
        return ResponseUtil.ok(iTranslateService.simpleToTraditional(str));
    }
}
