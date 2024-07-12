package com.jjoaquin3;

import com.jjoaquin3.endpoint.CurrencyEndpoint;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
    public static void main(String[] args)
    {
        CurrencyEndpoint.getInstance().showMenu();
    }
}
