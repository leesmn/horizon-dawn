package com.macro.mall.portal.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * 会员登录注册管理Controller
 * Created by macro on 2018/8/3.
 */
@Controller
@Api(tags = "UmsMemberController", description = "会员登录注册管理")
@RequestMapping("/sso")
public class UmsMemberController {
    @Autowired
    private UmsMemberService memberService;

    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation("注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult register(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String telephone,
                                 @RequestParam String authCode) {
        return memberService.register(username, password, telephone, authCode);
    }

    @ApiOperation("获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getAuthCode(@RequestParam String telephone) {
        return memberService.generateAuthCode(telephone);
    }

    @ApiOperation("修改密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePassword(@RequestParam String telephone,
                                 @RequestParam String password,
                                 @RequestParam String authCode) {
        return memberService.updatePassword(telephone,password,authCode);
    }

    @ApiOperation("通过微信code登录")
    @RequestMapping(value = "/wxappLogin", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@RequestParam String code) {
        // 微信小程序ID
        String appid = "wx716adfd339133bee";
        // 微信小程序秘钥
        String secret = "9ff780c095ac526e091d6a4335de3229";

//        // 根据小程序穿过来的code想这个url发送请求
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSON.parseObject(str);


        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = "";
        String apiUrl = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",appid,secret,code);
        //JSONObject wxRsp = restTemplate.getForObject(apiUrl, JSONObject.class);
        String wxRsp = restTemplate.postForEntity(apiUrl, null, String.class).getBody();
        JSONObject jsonObject = JSON.parseObject(wxRsp);
        if(jsonObject.containsKey("openid")){
            openid = jsonObject.getString("openid");
            return memberService.wxLogin(openid);
        }
        return CommonResult.failed("无法获取获取微信openid，"+jsonObject.getString("errmsg"));
    }

    @ApiOperation("通过微信code注册")
    @RequestMapping(value = "/wxappRegister", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult register(@RequestParam String code,@RequestParam String username,@RequestParam String pwd) {
        // 微信小程序ID
        String appid = "wx716adfd339133bee";
        // 微信小程序秘钥
        String secret = "9ff780c095ac526e091d6a4335de3229";
        String openid = "";
        String apiUrl = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",appid,secret,code);
        //JSONObject wxRsp = restTemplate.getForObject(apiUrl, JSONObject.class);
        String wxRsp = restTemplate.postForEntity(apiUrl, null, String.class).getBody();
        JSONObject jsonObject = JSON.parseObject(wxRsp);
        if(jsonObject.containsKey("openid")) {
            openid = jsonObject.getString("openid");
            return memberService.wxRegister(openid,username,pwd);
        }
        return CommonResult.failed("无法获取获取微信openid，"+jsonObject.getString("errmsg"));
    }


    @ApiOperation(value = "调用远程用户服务", notes = "调用远程用户服务")
    @RequestMapping(value = "/checkToken", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult checkToken(@RequestParam String token) throws IOException {

        return CommonResult.success("");
    }


}
