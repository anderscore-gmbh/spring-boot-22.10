package boot.fragments.jpa;

import java.time.Instant;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.Test;

import boot.backend.repository.TaskEntity;
import boot.backend.repository.TaskEntity.State;

/**
 * Verwendet die META-INF/persistence.xml
 */
public class PlainJpaTest {
    
    @Test
    public void testJpaQuery() {
        // tag::em[]
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("Tasks"); // Name der Persistence Unit
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("select t from TaskEntity t");
        List<TaskEntity> tasks = query.getResultList();
        tasks.forEach(System.out::println);
        // end::em[]
    }

    @Test
    public void testCriteriaApiQuery() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("Tasks");
        EntityManager em = factory.createEntityManager();

        // tag::crit[]
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TaskEntity> q = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> from = q.from(TaskEntity.class);
        q.select(from);
        
        TypedQuery<TaskEntity> query = em.createQuery(q);
        List<TaskEntity> tasks = query.getResultList();
        tasks.forEach(System.out::println);
        // end::crit[]
    }

    @Test
    public void insertData() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("Tasks");
        EntityManager em = factory.createEntityManager();
        TaskEntity task = new TaskEntity();
        task.setDateDue(Instant.now());
        task.setDescription("Learn JPA");
        task.setState(State.STARTED);

        em.getTransaction().begin();
        em.merge(task);
        em.getTransaction().commit();
    }
}
