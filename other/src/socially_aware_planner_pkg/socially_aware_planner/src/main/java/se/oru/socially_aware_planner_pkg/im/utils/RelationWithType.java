package se.oru.socially_aware_planner_pkg.im.utils;

import java.util.ArrayList;
import java.util.List;

public class RelationWithType<T, S, K> {
	private List<Pair<Pair<T, S>, K>> relation;
	// private K relationValue;

	public RelationWithType() {
		this.relation = new ArrayList<Pair<Pair<T, S>, K>>();
	}

	public void addRelation(T el1, S el2, K relationTypeObject) {
		Pair<T,S> elementsPair = new Pair<T,S>(el1, el2);
		Pair<Pair<T,S>, K> typePair = new Pair<Pair<T,S>, K>(elementsPair, relationTypeObject);
		relation.add(typePair);
	}
	
	public List<Pair<Pair<T, S>, K>> getRelation() {
		return relation;
	}
	

//	public boolean containsPair(T obj1, S obj2) {
//		return relation.contains(new Pair<T, S>(obj1, obj2));
//	}
	
//	public K getRelationValue() {
//		return relationValue;
//	}
	
//	// Get K Type if needed
//	private Class<K> relationType;
//	public RelationWithType(Class<K> type) {
//         this.relationType = type;
//    }
//	public Class<K> getRelationType() {
//         return this.relationType;
//    }

}
