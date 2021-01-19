
/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 19 OCT 2020
 * 
 */

import java.io.Serializable;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

public class IdGeneratorUtil implements IdentifierGenerator{

    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {
                return RandomStringUtils.randomAlphanumeric(16);
            }

}