package me.geso.avans;

@FunctionalInterface
public interface AvansAction {
	public AvansResponse run(AvansWebApplication web);
}
