package net.meisen.gwt.cbwaf.client.entry;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import net.meisen.gwt.cbwafapi.client.ApiHelper;
import net.meisen.gwt.cbwafshare.share.SharedStuff;

public class CoreEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {

		final Label label = new Label("Hello GWT !!!");
		final Button button = new Button("Say something");

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ApiHelper.sayMyName();
        
        Window.alert(SharedStuff.getMyName());
			}
		});

		RootPanel.get().add(label);
		RootPanel.get().add(button);
	}

}
