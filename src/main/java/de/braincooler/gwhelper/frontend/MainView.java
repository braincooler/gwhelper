package de.braincooler.gwhelper.frontend;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.braincooler.gwhelper.consumer.GwConsumer;

import java.util.Map;

@Route
public class MainView extends VerticalLayout {

    private GwConsumer gwConsumer;

    public MainView(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
        init();
    }

    private void init() {

        Map<String, Integer> targetStrings = gwConsumer.get1635TargetStrings();
        targetStrings.keySet().forEach(s -> {
            String link = "http://www.gwars.ru" + s;
            if (gwConsumer.getOwnerSindikat(s) != targetStrings.get(s)) {
                Anchor anchor = new Anchor(link);
                anchor.setText(String.valueOf(targetStrings.get(s)));
                anchor.setTarget("_blank");
                add(anchor);
            }
        });
    }
}