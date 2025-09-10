package eci.edu.arep.microspringboot.examples;

import eci.edu.arep.microspringboot.annotations.GetMapping;
import eci.edu.arep.microspringboot.annotations.RequestParam;
import eci.edu.arep.microspringboot.annotations.RestController;

@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return "Hola " + name;
    }
}
