package org.jumpmind.pos.persist;

import org.jumpmind.pos.persist.cars.CarModel;
import org.jumpmind.pos.persist.cars.TestPersistCarsConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistCarsConfig.class})
public class DBSessionDmlTest {

    @Autowired
    private DBSessionFactory sessionFactory;

    final static String VIN = "KMHCN46C58U242743";


    @Before
    public void setup() {
        {
            DBSession db = sessionFactory.createDbSession();
            db.executeSql("TRUNCATE TABLE CAR_CAR");
        }


        {
            DBSession db = sessionFactory.createDbSession();
            CarModel someHyundai = new CarModel();
            someHyundai.setVin(VIN);
            someHyundai.setMake("Hyundai");
            someHyundai.setModel("Accent");
            someHyundai.setModelYear("2005");
            someHyundai.setIsoCurrencyCode("USD");
            db.save(someHyundai);
        }
        {
            DBSession db = sessionFactory.createDbSession();
            CarModel someHyundai = new CarModel();
            someHyundai.setVin(VIN + "2342");
            someHyundai.setMake("Hyundai");
            someHyundai.setModel("Elantra");
            someHyundai.setModelYear("2006");
            someHyundai.setIsoCurrencyCode("USD");
            db.save(someHyundai);
        }
        {
            DBSession db = sessionFactory.createDbSession();
            CarModel someToyota = new CarModel();
            someToyota.setVin(VIN + "4764");
            someToyota.setMake("Toyota");
            someToyota.setModel("RAV4");
            someToyota.setModelYear("2014");
            someToyota.setIsoCurrencyCode("USD");
            db.save(someToyota);
        }
        {
            DBSession db = sessionFactory.createDbSession();
            CarModel someFord = new CarModel();
            someFord.setVin(VIN + "1297");
            someFord.setMake("Ford");
            someFord.setModel("Escape");
            someFord.setModelYear("2010");
            someFord.setIsoCurrencyCode("USD");
            db.save(someFord);
        }
        {
            DBSession db = sessionFactory.createDbSession();
            CarModel antiqueFord = new CarModel();
            antiqueFord.setVin(VIN + "3578");
            antiqueFord.setMake("Ford");
            antiqueFord.setModel("Model T");
            antiqueFord.setModelYear("1919");
            antiqueFord.setAntique(true);
            antiqueFord.setIsoCurrencyCode("USD");
            db.save(antiqueFord);
        }
    }

    @Test
    public void testUpdateWithAllOptionalWhere() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("year", "2020");
        params.put("make", "Hyundai");
        params.put("model", "Accent");
        int numRowsUpdated = db.executeDml("updateModelYear", params);
        assertEquals(1, numRowsUpdated);
    }

    @Test
    public void testUpdateWithNoOptionalWhere() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("year", "2021");
        int numRowsUpdated = db.executeDml("updateModelYear", params);
        assertEquals(5, numRowsUpdated);
    }

    @Test
    public void testUpdateWithWhereAndOptionalWhereParam() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("vin", VIN + 1111);
        params.put("make", "Ford");
        params.put("antique", true);
        int numRowsUpdated = db.executeDml("updateVIN", params);
        assertEquals(1, numRowsUpdated);
    }

    @Test
    public void testUpdateWithWhereAndNoOptionalWhereParam() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("vin", VIN + 4589);
        params.put("make", "Toyota");
        int numRowsUpdated = db.executeDml("updateVIN", params);
        assertEquals(1, numRowsUpdated);
    }

    @Test
    public void testUpdateWithOnlyWhere() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("value", 25000);
        params.put("make", "Toyota");
        params.put("model", "RAV4");
        int numRowsUpdated = db.executeDml("updateValue", params);
        assertEquals(1, numRowsUpdated);
    }

    @Test
    public void testNoUpdate() {
        DBSession db = sessionFactory.createDbSession();
        Map<String, Object> params = new HashMap<>();
        params.put("year", "2013");
        params.put("make", "Chevy");
        int numRowsUpdated = db.executeDml("updateModelYear", params);
        assertEquals(0, numRowsUpdated);
    }
}
