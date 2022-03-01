package org.jumpmind.pos.core.screen;


import java.util.ArrayList;
import java.util.List;

import org.jumpmind.pos.core.model.Form;
import org.jumpmind.pos.core.model.MultipleFormOption;

public class MultipleDynamicFormScreen extends Screen{

	private static final long serialVersionUID = 1L;

	private List<MultipleFormOption> forms = new ArrayList<MultipleFormOption>();
	private Form submittedForm;
	
	public MultipleDynamicFormScreen() {
		setScreenType(ScreenType.MultipleDynamicForm);
	}

	public List<MultipleFormOption> getForms() {
		return forms;
	}

	public void setForms(List<MultipleFormOption> forms) {
		this.forms = forms;
	}

	public void addForm( MultipleFormOption form ) {
		this.forms.add(form);
	}

	
	public void setForm(Form form) {
		submittedForm = form;
	}

	public Form getForm() {
		return submittedForm;
	}
	
}

