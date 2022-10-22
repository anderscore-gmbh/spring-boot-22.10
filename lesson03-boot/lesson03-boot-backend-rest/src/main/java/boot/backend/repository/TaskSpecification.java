package boot.backend.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

// tag::code[]
public class TaskSpecification implements Specification<TaskEntity> {

    private final TaskSearchCriteria criteria;
    
    public TaskSpecification(TaskSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<TaskEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        query.orderBy(builder.desc(root.get("dateDue")));
        return builder.equal(root.get("state"), criteria.getState());
    }
}
// end::code[]