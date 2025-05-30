package com.example.application.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;


@Push // Move the @Push annotation here
@Theme("default")
public class AppShell implements AppShellConfigurator {
    // This class can be empty if you only need to configure annotations
}