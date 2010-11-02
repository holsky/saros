package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryComposite;

public class ExplanationOnlySimpleExplanatoryCompositeDemo extends Demo {
	public ExplanationOnlySimpleExplanatoryCompositeDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		final SimpleExplanatoryComposite explanatoryComposite = new SimpleExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the simple explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				SimpleExplanation expl = new SimpleExplanation(null, text);
				explanatoryComposite.showExplanation(expl);

				Display.getCurrent().timerExec(5000, new Runnable() {

					@Override
					public void run() {
						explanatoryComposite.hideExplanation();
					}

				});
			}

		});
	}
}