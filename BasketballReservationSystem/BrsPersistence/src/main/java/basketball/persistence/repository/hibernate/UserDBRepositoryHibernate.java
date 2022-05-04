package basketball.persistence.repository.hibernate;

import basketball.model.User;
import basketball.model.validator.IValidator;
import basketball.model.validator.ValidatorException;
import basketball.persistence.IUserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Properties;

public class UserDBRepositoryHibernate implements IUserRepository {
    static SessionFactory sessionFactory;
    private final IValidator<User> validator;
    private static final Logger logger= LogManager.getLogger();

    public UserDBRepositoryHibernate(Properties props, IValidator<User> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing AccountDBRepositoryHibernate with properties: {} ", props);
        this.validator = validator;
    }

    @Override
    public void save(User user) throws ValidatorException {
        validator.validate(user);
        logger.traceEntry("saving user {} ", user);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(user);
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
        logger.traceEntry("Removing user {} ", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                User toDelete = new User();
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
                size = (Long)session.createQuery("select count(all User) from User").getSingleResult();
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
    public User findOne(Long id) {
        logger.traceEntry();

        User result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from User where id = ?", User.class)
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
    public Iterable<User> findAll() {
        logger.traceEntry();

        List<User> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from User", User.class).list();
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
    public void update(User user) throws ValidatorException {
        validator.validate(user);
        logger.traceEntry("Updating user {} ", user);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(user);
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
    public User findBy(String username, String password) {
        logger.traceEntry();

        User result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from User where username = ? and password = ?", User.class)
                        .setParameter(0, username)
                        .setParameter(1, password)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find by error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
        return result;
    }
}
