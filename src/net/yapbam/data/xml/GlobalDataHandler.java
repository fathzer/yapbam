package net.yapbam.data.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.yapbam.data.*;
import net.yapbam.date.helpers.DateStepper;
import net.yapbam.date.helpers.DayDateStepper;
import net.yapbam.date.helpers.DeferredValueDateComputer;
import net.yapbam.date.helpers.MonthDateStepper;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class GlobalDataHandler extends DefaultHandler {
	private GlobalData data;
	// used to save temporary object data
	private Stack<Object> tempData;

	GlobalDataHandler(GlobalData data) {
		super();
		this.data = data;
		this.tempData = new Stack<Object>();
	}
	
	private Map<String, String> buildMap(Attributes attributes) {
		Map<String, String> result = new HashMap<String, String>();
		for (int i=0;i<attributes.getLength();i++) {
			result.put(attributes.getLocalName(i), attributes.getValue(i));
		}
		return result;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals(Serializer.GLOBAL_DATA_TAG)) {
		} else if (qName.equals(Serializer.ACCOUNT_TAG)) {
			String id = attributes.getValue(Serializer.ID_ATTRIBUTE);
			double balance = Double.parseDouble(attributes.getValue(Serializer.INITIAL_BALANCE_ATTRIBUTE));
			Account account = new Account(id, balance);
			this.data.add(account);
			AlertThreshold alertThreshold;
			if (Boolean.parseBoolean(attributes.getValue(Serializer.ALERT_DISABLED))) {
				alertThreshold = AlertThreshold.NO;
			} else {
				if (attributes.getValue(Serializer.ALERT_DIRECTION)==null) {
					// No alert specified => alert if less than 0.
					alertThreshold = new AlertThreshold(0, true);
				} else {
					alertThreshold = new AlertThreshold(Double.parseDouble(attributes.getValue(Serializer.ALERT_THRESHOLD)),
							Boolean.parseBoolean(attributes.getValue(Serializer.ALERT_DIRECTION)));
				}
			}
			this.data.setAlertThreshold(account, alertThreshold);
			this.tempData.push(account);
		} else if (qName.equals(Serializer.CATEGORY_TAG)) {
			String id = attributes.getValue(Serializer.ID_ATTRIBUTE);
			Category cat = new Category(id);
			this.data.add(cat);
		} else if (qName.equals(Serializer.MODE_TAG)) {
			this.tempData.push(attributes.getValue(Serializer.ID_ATTRIBUTE));
			this.tempData.push(attributes.getValue(Serializer.CHECKBOOK_ATTRIBUTE)!=null?true:false);
			this.tempData.push(new DateStepper[2]);
		} else if (qName.equals(Serializer.CHECKBOOK_TAG)) {
			String prefix = attributes.getValue(Serializer.PREFIX_ATTRIBUTE);
			BigInteger first = new BigInteger(attributes.getValue(Serializer.FIRST_NUMBER_ATTRIBUTE));
			String value = attributes.getValue(Serializer.NEXT_NUMBER_ATTRIBUTE);
			BigInteger next = value==null?null:new BigInteger(value);
			int size = Integer.parseInt(attributes.getValue(Serializer.SIZE_ATTRIBUTE));
			this.tempData.push (new Checkbook(prefix, first, size, next));
		} else if (qName.equals(Serializer.EXPENSE_VDC_TAG) || qName.equals(Serializer.RECEIPT_VDC_TAG)) {
			DateStepper vdc;
			String kind = attributes.getValue(Serializer.DATE_STEPPER_KIND_ATTRIBUTE);
			if ((kind == null) || kind.equals(Serializer.IMMEDIATE_DATE_STEPPER_KIND)) {
				vdc = DateStepper.IMMEDIATE;
			} else if (kind.equals(Serializer.RELATIVE_DATE_STEPPER_KIND)) {
				int delay = Integer.parseInt(attributes.getValue(Serializer.PERIOD_ATTRIBUTE));
				vdc = new DayDateStepper(delay, null);
			} else if (kind.equals(Serializer.DEFERRED_DATE_STEPPER_KIND)) {
				int stopDay = Integer.parseInt(attributes.getValue(Serializer.STOP_DAY_ATTRIBUTE));
				int debtDay = Integer.parseInt(attributes.getValue(Serializer.DEBT_DAY_ATTRIBUTE));
				vdc = new DeferredValueDateComputer(stopDay, debtDay);
			} else {
				throw new RuntimeException("Invalid ValueDateComputer kind : "+kind);
			}
			DateStepper[] vdcs = (DateStepper[]) this.tempData.peek();
			int index = qName.equals(Serializer.EXPENSE_VDC_TAG) ? 0 : 1;
			if (vdcs[index]!=null) System.err.println("too much value date computer");//LOG
			vdcs[index] = vdc;
		} else if (qName.equals(Serializer.TRANSACTION_TAG)) {
			//We can't directly push the attributes because SAX may reuse the same instance to store next element's attributes.
			this.tempData.push(buildMap(attributes));
			this.tempData.push(new ArrayList<SubTransaction>());
		} else if (qName.equals(Serializer.SUBTRANSACTION_TAG)) {
			double amount = Double.parseDouble(attributes.getValue(Serializer.AMOUNT_ATTRIBUTE));
			String description = attributes.getValue(Serializer.DESCRIPTION_ATTRIBUTE);
			String categoryId = attributes.getValue(Serializer.CATEGORY_ATTRIBUTE);
			Category category = this.data.getCategory(categoryId);
			SubTransaction sub = new SubTransaction(amount, description, category);
			ArrayList<SubTransaction> lst = (ArrayList<SubTransaction>) this.tempData.peek();
			lst.add(sub);
		} else if (qName.equals(Serializer.PERIODICAL_TAG)) {
			//We can't directly push the attributes because SAX may reuse the same instance to store next element's attributes.
			this.tempData.push(buildMap(attributes));
			this.tempData.push(null); //To store the date stepper
			this.tempData.push(new ArrayList<SubTransaction>());
		} else if (qName.equals(Serializer.DATE_STEPPER_TAG)) {
			String kind = attributes.getValue(Serializer.DATE_STEPPER_KIND_ATTRIBUTE);
			DateStepper stepper;
			if (kind.equals(Serializer.MONTHLY_DATE_STEPPER_KIND)) {
				int period = Integer.parseInt(attributes.getValue(Serializer.PERIOD_ATTRIBUTE));
				if (period<=0) throw new IllegalArgumentException();
				int day = Integer.parseInt(attributes.getValue(Serializer.DAY_ATTRIBUTE));
				String dummy =  attributes.getValue(Serializer.LAST_DATE_ATTRIBUTE);
				Date lastDate = dummy==null?null:Serializer.toDate(dummy);
				stepper = new MonthDateStepper(period, day, lastDate);
			} else if (kind.equals(Serializer.RELATIVE_DATE_STEPPER_KIND)) {
				int period = Integer.parseInt(attributes.getValue(Serializer.PERIOD_ATTRIBUTE));
				String dummy =  attributes.getValue(Serializer.LAST_DATE_ATTRIBUTE);
				Date lastDate = dummy==null?null:Serializer.toDate(dummy);
				stepper = new DayDateStepper(period, lastDate);
			} else {
				throw new IllegalArgumentException("Unknown date stepper : "+kind);
			}
			Object obj = this.tempData.pop(); // The subtransaction list, will be returned in the stack in a few lines
			Object old = this.tempData.pop();
			this.tempData.push(stepper);
			this.tempData.push(obj);
			if (old!=null) throw new IllegalStateException("Two date steppers found"); // Hu ! there are two date steppers !!!
		} else {
			throw new IllegalArgumentException ("Unknown tag "+qName);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals(Serializer.GLOBAL_DATA_TAG)) {
		} else if (qName.equals(Serializer.ACCOUNT_TAG)) {
			this.tempData.pop(); // remove the tag we added in the stack
		} else if (qName.equals(Serializer.CATEGORY_TAG)) {
		} else if (qName.equals(Serializer.MODE_TAG)) {
			DateStepper[] vdcs = (DateStepper[]) this.tempData.pop();
			boolean useCheckbook = (Boolean) this.tempData.pop();
			String id = (String) this.tempData.pop();
			Mode mode = new Mode(id, vdcs[1], vdcs[0], useCheckbook);
			Account account = (Account) this.tempData.peek();
			this.data.add(account, mode);
		} else if (qName.equals(Serializer.CHECKBOOK_TAG)) {
			Checkbook book = (Checkbook) this.tempData.pop();
			Account account = (Account) this.tempData.peek();
			this.data.add(account, book);
		} else if (qName.equals(Serializer.EXPENSE_VDC_TAG)) {
		} else if (qName.equals(Serializer.RECEIPT_VDC_TAG)) {
		} else if (qName.equals(Serializer.TRANSACTION_TAG)) {
			ArrayList<SubTransaction> lst = (ArrayList<SubTransaction>) this.tempData.pop();
			Map<String, String> attributes = (Map<String, String>) this.tempData.pop();
			PartialTransaction p = new PartialTransaction(this.data, attributes);		
			Date date = Serializer.toDate(attributes.get(Serializer.DATE_ATTRIBUTE));
			String number = attributes.get(Serializer.NUMBER_ATTRIBUTE);
			Date valueDate = Serializer.toDate(attributes.get(Serializer.VALUE_DATE_ATTRIBUTE));
			String statement = attributes.get(Serializer.STATEMENT_ATTRIBUTE);
			this.data.add(new Transaction(date, number, p.description, p.amount, p.account, p.mode, p.category, valueDate, statement, lst));
		} else if (qName.equals(Serializer.SUBTRANSACTION_TAG)) {
		} else if (qName.equals(Serializer.PERIODICAL_TAG)) {
			ArrayList<SubTransaction> lst = (ArrayList<SubTransaction>) this.tempData.pop();
			DateStepper stepper = (DateStepper) this.tempData.pop();
			Map<String, String> attributes = (Map<String, String>) this.tempData.pop();
			PartialTransaction p = new PartialTransaction(this.data, attributes);
			String attribute = attributes.get(Serializer.NEXT_DATE_ATTRIBUTE);
			Date nextDate = attribute==null?null:Serializer.toDate(attribute);
			boolean enabled = Boolean.parseBoolean(attributes.get(Serializer.ENABLED_ATTRIBUTE));
			this.data.add(new PeriodicalTransaction(p.description, p.amount, p.account, p.mode, p.category, lst, nextDate, enabled, stepper));
		} else if (qName.equals(Serializer.DATE_STEPPER_TAG)) {
		} else {
			System.err.println ("Unknown tag "+qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String str = new String(ch, start, length);
		str = str.trim();
		if (str.length()!=0) {
			System.err.println ("strange, characters is called : "+str);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		this.data.clear();
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
}
