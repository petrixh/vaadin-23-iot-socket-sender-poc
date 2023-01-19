package com.example.application.views.helloworld;

import com.example.application.service.IotService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.util.Optional;

@PageTitle("Hello World")
@Route(value = "hello")
@RouteAlias(value = "")
public class HelloWorldView extends VerticalLayout implements IotService.Observer {

    private final IotService ioTService;
    private final HorizontalLayout form;
    private final VerticalLayout clientResopnses;
    private TextField name;
    private Button sayHello;

    public HelloWorldView(IotService ioTService) {
        this.ioTService = ioTService;

        form = new HorizontalLayout();
        add(form);

        clientResopnses = new VerticalLayout();
        add(clientResopnses);

        form.add(name = new TextField("Your name"));
        form.add(sayHello = new Button("Say hello"));
        sayHello.addClickListener(e -> {
            String message = "Hello " + name.getValue();
            Notification.show(message);
            ioTService.sendMessage(message);
        });
        sayHello.addClickShortcut(Key.ENTER);
        form.setVerticalComponentAlignment(Alignment.END, name, sayHello);
        setMargin(true);

        // Using simple observer pattern for POC...
        // published events probably wiser for a real system
        addAttachListener(attachEvent -> {
            ioTService.registerObserver(this);
        });

        addDetachListener(detachEvent -> {
            ioTService.removeObserver(this);
        });
    }


    @Override
    public void onMessage(String message) {

        Optional<UI> uiOptional = getUI();
        //Only update UI if attached (visible)
        if (uiOptional.isPresent()) {
            uiOptional.get().access(() -> {
                // Text text = new Text(message);
                // Using Divs just to make it look prettier as text is inline
                // and multiple after each other becomes hard to read...
                Div text = new Div();
                text.setText(message);
                // Add the responses in reverse so that the
                // topmost is always the last message for easier reading...
                clientResopnses.addComponentAsFirst(text);
            });
        }

    }
}
