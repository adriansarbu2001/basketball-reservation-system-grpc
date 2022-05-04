package basketball.persistence.repository.hibernate;

import basketball.model.Match;
import basketball.model.validator.IValidator;
import basketball.model.validator.ValidatorException;
import basketball.persistence.IMatchRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Properties;

public class MatchDBRepositoryHibernate implements IMatchRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Match> validator;
    private static final Logger logger = LogManager.getLogger();

    public MatchDBRepositoryHibernate(Properties props, IValidator<Match> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing MatchDBRepositoryHibernate with properties: {} ", props);
        this.validator = validator;
    }

    @Override
    public void save(Match match) throws ValidatorException {
        validator.validate(match);
        logger.traceEntry("saving match {} ", match);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(match);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Insert error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
    }

    @Override
    public void remove(Long id) {
        logger.traceEntry("Removing match {} ", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Match toDelete = new Match();
                toDelete.setId(id);
                session.delete(toDelete);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Remove error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
    }

    @Override
    public Long size() {
        logger.traceEntry();

        Long size = -1L;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                size = (Long)session.createQuery("select count(all Match) from Match").getSingleResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Size error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
        return size;
    }

    @Override
    public Match findOne(Long id) {
        logger.traceEntry();

        Match result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Match where id = ?", Match.class)
                        .setParameter(0, id)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find one error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public Iterable<Match> findAll() {
        logger.traceEntry();

        List<Match> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Match", Match.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public void update(Match match) throws ValidatorException {
        validator.validate(match);
        logger.traceEntry("Updating match {} ", match);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(match);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Update error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
    }

    @Override
    public List<Match> availableMatchesDescending() {
        logger.traceEntry();

        List<Match> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction(); // select * from Matches where no_available_seats > 0 order by no_available_seats desc
                result = session.createQuery("from Match where noAvailableSeats > 0 order by noAvailableSeats desc", Match.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
        return result;
    }
}
