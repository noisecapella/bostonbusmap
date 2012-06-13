package boston.Bus.Map.data;

import boston.Bus.Map.util.StringUtil;

public class TitleAnd<T> implements Comparable<TitleAnd<T>>{
	private final String title;
	private final T other;
	
	public TitleAnd(String title, T other) {
		this.title = title;
		this.other = other;
	}
	
	public T getOther() {
		return other;
	}
	public String getTitle() {
		return title;
	}

	@Override
	public int compareTo(TitleAnd<T> another) {
		return title.compareToIgnoreCase(another.title);
	}
}
