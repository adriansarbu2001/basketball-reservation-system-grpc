package basketball.persistence.repository.hibernate;

import basketball.model.Ticket;
import basketball.model.validator.IValidator;
import basketball.model.validator.ValidatorException;
import basketball.persistence.ITicketRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Properties;

public class TicketDBRepositoryHibernate implements ITicketRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Ticket> validator;
    private static final Logger logger= LogManager.getLogger();

    public TicketDBRepositoryHibernate(Properties props, IValidator<Ticket> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing TicketDBRepositoryHibernate with properties: {} ", props);
        this.validator = validator;
    }

    @Override
    public void save(Ticket ticket) throws ValidatorException {
        validator.validate(ticket);
        logger.traceEntry("saving ticket {} ", ticket);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(ticket);
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
        logger.traceEntry("Removing ticket {} ", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Ticket toDelete = new Ticket();
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
                size = (Long)session.createQuery("select count(all Ticket) from Ticket").getSingleResult();
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
    public Ticket findOne(Long id) {
        logger.traceEntry();

        Ticket result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Ticket where id = ?", Ticket.class)
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
    public Iterable<Ticket> findAll() {
        logger.traceEntry();

        List<Ticket> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Ticket", Ticket.class).list();
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
    public void update(Ticket ticket) throws ValidatorException {
        validator.validate(ticket);
        logger.traceEntry("Updating ticket {} ", ticket);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(ticket);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Update error " + ex);
                if (tx != null)
                    tx.rollback();
            }
        }

        logger.traceExit();
    }
}
