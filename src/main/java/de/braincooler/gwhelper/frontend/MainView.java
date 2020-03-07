package de.braincooler.gwhelper.frontend;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.braincooler.gwhelper.consumer.GwConsumer;

@Route
public class MainView extends VerticalLayout {

    private GwConsumer gwConsumer;

    public MainView(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
        add(new Text(String.valueOf(gwConsumer.getRibaMinPrice())));
    }
}