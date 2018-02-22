package com.github.mauro1855.ocrservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by mauro1855 on 01/12/2016.
 */
@Controller
public class UIController {

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home()
  {
    return "index";
  }

}
