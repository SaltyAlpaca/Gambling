package de.salty.smp.gambling;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomAPI<E> {
	
    public final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomAPI() {
        this(new Random());
    }

    public RandomAPI(Random random) {
        this.random = random;
    }

    public void add(double weight, E result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }

    public WinningObject<E> next() {
        double value = random.nextDouble() * total;
        return new WinningObject<E>(map.ceilingEntry(value).getValue(), value);
    }
    
    @SuppressWarnings("unchecked")
	public RandomAPI<E> clone(){
    	try {
			return (RandomAPI<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public void clear(){
    	map.clear();
    	total = 0;
    }
    
    public static class WinningObject<E>{
    	public E entry;
    	public double ticket;
    	public WinningObject(E e, double d){
    		this.entry = e;
    		this.ticket = d;
    	}
    }
}