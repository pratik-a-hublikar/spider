package com.spider.common.util;

import com.spider.common.enums.Operator;
import com.spider.common.exception.FilterException;
import com.spider.common.request.filter.FilterCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class CriteriaUtil<D> {


    public Specification<D> toPredicate(final List<FilterCriteria> andCriteriaList, final List<FilterCriteria> orCriteriaList){
       return (Root<D> root, CriteriaQuery<?> cq,CriteriaBuilder cb) ->{
           ArrayList<Predicate> predicateList = new ArrayList<>();
           if(!CollectionUtils.isEmpty(andCriteriaList)){
               andCriteriaList.forEach(criteria -> predicateList.add(getPredicate(criteria,root,cb)));
           }
           if(!CollectionUtils.isEmpty(orCriteriaList)){
               ArrayList<Predicate> orPredicateList = new ArrayList<>();
               orCriteriaList.forEach(criteria -> orPredicateList.add(getPredicate(criteria,root,cb)));
               if(!orPredicateList.isEmpty()){
                   Predicate orPredicate = cb.or(orPredicateList.toArray(new Predicate[0]));
                   Predicate andPredicate = cb.and(predicateList.toArray(new Predicate[0]));
                   return cb.and(orPredicate,andPredicate);
               }
           }
           return cb.and(predicateList.toArray(new Predicate[0]));
       };
    }

    public Predicate getPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder){
        Predicate predicate;
        Operator operator = Operator.getOperator(filterCriteria.getOperator());
        if(operator == null){
            throw new FilterException("filter.invalid.operator");
        }
        predicate = switch (operator) {
            case Operator.CONTAINS -> getContainPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.EQUAL -> getEqualPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.NOT_EQUAL -> getNotEqualPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.IN -> getInPredicate(filterCriteria, dRoot);
            case Operator.GREATER_THAN -> getGreaterThanPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.LESS_THAN -> getLessThanPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.GREATER_THAN_EQUAL -> getGreaterThanEqualsPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.LESS_THAN_EQUAL -> getLessThanEqualsPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.TRUE -> getTruePredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.FALSE -> getFalsePredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.NULL -> getNullPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.NOT_NULL -> getNotNullPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.CONTAINS_OR_EQUAL -> getContainOrEqualPredicate(filterCriteria, dRoot, criteriaBuilder);
            case Operator.BETWEEN -> getBetweenPredicate(filterCriteria, dRoot, criteriaBuilder);
        };
        if(null == predicate){
            throw new FilterException("filter.operator.unsupported");
        }
        return predicate;

    }

    private Predicate getContainOrEqualPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        ArrayList<Predicate> predicates = new ArrayList<>();
        filterCriteria.getValues().forEach(value -> {
            Expression<String> field = criteriaBuilder.lower(dRoot.get(filterCriteria.getColumn()).as(String.class));
            String normalizedValue = value.toLowerCase(Locale.ROOT);
            predicates.add(criteriaBuilder.like(field,getContainsEnd(normalizedValue)));
            predicates.add(criteriaBuilder.like(field,getContainsStart(normalizedValue)));
            predicates.add(criteriaBuilder.like(field,getContainsMiddle(normalizedValue)));
            predicates.add(criteriaBuilder.like(field,normalizedValue));
        });
        return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    }

    private Predicate getContainPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        List<Predicate> predicateList=new ArrayList<>();
        Expression<String> field = criteriaBuilder.lower(dRoot.get(filterCriteria.getColumn()).as(String.class));
        filterCriteria.getValues().forEach(value -> predicateList.add(criteriaBuilder.like(field,getContainsValue(value.toLowerCase(Locale.ROOT)))));
        return criteriaBuilder.or(predicateList.toArray(new Predicate[0]));
    }

    private Predicate getEqualPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.equal(dRoot.get(filterCriteria.getColumn()),value);
    }

    private Predicate getNotEqualPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.notEqual(dRoot.get(filterCriteria.getColumn()),value);
    }
    private Predicate getInPredicate(FilterCriteria filterCriteria, Root<D> dRoot) {
        validateInput(filterCriteria.getValues());
        Stream<Object> objectStream = filterCriteria.getValues().stream().filter(Objects::nonNull).map(value -> getValue(dRoot.get(filterCriteria.getColumn()), value));
        return dRoot.get(filterCriteria.getColumn()).in(objectStream.toList());
    }


    private Predicate getGreaterThanPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.greaterThan(dRoot.get(filterCriteria.getColumn()), (Comparable) value);
    }

    private Predicate getLessThanPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.lessThan(dRoot.get(filterCriteria.getColumn()), (Comparable) value);
    }
    private Predicate getGreaterThanEqualsPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.greaterThanOrEqualTo(dRoot.get(filterCriteria.getColumn()), (Comparable) value);
    }

    private Predicate getLessThanEqualsPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        Object value = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        return criteriaBuilder.lessThanOrEqualTo(dRoot.get(filterCriteria.getColumn()), (Comparable) value);
    }


    private Predicate getTruePredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.isTrue(dRoot.get(filterCriteria.getColumn()));
    }
    private Predicate getFalsePredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.isFalse(dRoot.get(filterCriteria.getColumn()));
    }

    private Predicate getNotNullPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.isNotNull(dRoot.get(filterCriteria.getColumn()));
    }

    private Predicate getNullPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.isNull(dRoot.get(filterCriteria.getColumn()));
    }

    private Predicate getBetweenPredicate(FilterCriteria filterCriteria, Root<D> dRoot, CriteriaBuilder criteriaBuilder) {
        validateInput(filterCriteria.getValues());
        // Expecting two values for the range
        if (filterCriteria.getValues().size() != 2) {
            throw new FilterException("filter.between.values");

        }

        Object from = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(0));
        Object to = getValue(dRoot.get(filterCriteria.getColumn()), filterCriteria.getValues().get(1));

        return criteriaBuilder.between(dRoot.get(filterCriteria.getColumn()), (Comparable) from, (Comparable) to);
    }

    private void validateInput(List<String> values) {
        if(CollectionUtils.isEmpty(values)){
           throw new FilterException("filter.values.null");
        }
    }

    private String getContainsValue(String value) {
        return "%".concat(value.toLowerCase()).concat("%");
    }

    private String getContainsMiddle(String value) {
        return "%,".concat(value).concat(",%");
    }
    private String getContainsStart(String value) {
        return value.concat(",%");
    }

    private String getContainsEnd(String value) {
        return "%,".concat(value);
    }

    private synchronized Object getValue(Path<D> path, String value) {
        try {
            if (path.getJavaType().getName().equals("java.util.Date")) {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(value);
            }
            return value;
        } catch (ParseException ex) {
            throw new RuntimeException("Failed to parse the data", ex);
        }
    }
}
