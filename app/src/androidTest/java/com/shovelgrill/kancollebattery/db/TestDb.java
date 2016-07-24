package com.shovelgrill.kancollebattery.db;

import android.test.AndroidTestCase;

/**
 * Created by qwerty on 21. 7. 2016.
 */
public class TestDb extends AndroidTestCase {

    private ShipsDbHelper db;

    private Ship HARUNA,SHIMAKAZE;

    private ImageSet HARUNA_CLASSIC,HARUNA_WINTER;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new ShipsDbHelper(getContext());

        HARUNA = new Ship();
        HARUNA.url_name = "Haruna";
        HARUNA.name = "Haruna";

        SHIMAKAZE = new Ship();
        SHIMAKAZE.url_name = "Shimakaze";
        SHIMAKAZE.name = "Shimakaze";

        HARUNA_CLASSIC = new ImageSet();
        HARUNA_CLASSIC.name = "HARUNA CLASSIC";
        HARUNA_CLASSIC.image_url = "haruna_classic.png";
        HARUNA_CLASSIC.image_url_dmg = "haruna_classic_damaged.png";
        HARUNA_CLASSIC.wiki_id = 750;

        HARUNA_WINTER = new ImageSet();
        HARUNA_WINTER.name = "HARUNA WINTER";
        HARUNA_WINTER.image_url = "haruna_winter.png";
        HARUNA_WINTER.image_url_dmg = "haruna_winter_damaged.png";
        HARUNA_WINTER.wiki_id = 739;

    }

    public void testShipOneItem() throws Throwable {
        assertNotNull("database should exist",db);
        db.addShip(HARUNA);
        Ship ship = db.getShips().get(0);
        assertEquals("Name of inserted ship should return back",HARUNA.name,ship.name);
    }

    public void testShipDelete() throws Throwable {
        db.addShip(HARUNA);
        db.deleteAllShips();
        assertEquals("Should get 0 columns",0,db.getShips().size());
    }

    public void testShipSecondItem() throws Throwable {
        db.deleteAllShips();
        db.addShip(HARUNA);
        db.addShip(SHIMAKAZE);
        Ship ship = db.getShips().get(db.getShips().size()-1);
        assertEquals("Name of inserted ship should return back",SHIMAKAZE.name,ship.name);
        assertEquals("return 2 ships",2,db.getShips().size());
    }

    public void testShipUniqueURL() throws Throwable {
        db.deleteAllShips();
        db.addShip(HARUNA);
        db.addShip(HARUNA);
        Ship ship = db.getShips().get(0);
        assertEquals("Should return only one column",1,db.getShips().size());
    }

    public void testShipFirstId() throws Throwable {
        db.addShip(HARUNA);
        db.deleteAllShips();
        db.addShip(SHIMAKAZE);
        db.deleteAllShips();
        db.addShip(HARUNA);
        Ship ship = db.getShips().get(0);
        assertEquals("ID Should be 1",1,ship.id);
    }

    public void testImageSetOneItem() throws Throwable {
        db.deleteAllShips();
        db.addShip(HARUNA);
        int haruna_id = db.getShips().get(0).id;
        HARUNA_CLASSIC.ship_id = haruna_id;
        db.addImageSet(HARUNA_CLASSIC);
        ImageSet imageSet = db.getImageSets().get(0);
        assertEquals("url should get back",HARUNA_CLASSIC.image_url,imageSet.image_url);
        assertEquals("dmg url should get back",HARUNA_CLASSIC.image_url_dmg,imageSet.image_url_dmg);
        assertEquals("id should get back",HARUNA_CLASSIC.wiki_id,imageSet.wiki_id);
        assertEquals("ship id should be same",haruna_id,imageSet.ship_id);
        assertEquals("ship name should be same",HARUNA.url_name,imageSet.ship_name);
    }

    public void testImageSetUnique() throws Throwable {
        db.deleteAllShips();
        db.deleteAllImageSets();
        db.addShip(HARUNA);
        int haruna_id = db.getShips().get(0).id;
        HARUNA_CLASSIC.ship_id = haruna_id;
        HARUNA_WINTER.ship_id = haruna_id;
        db.addImageSet(HARUNA_CLASSIC);
        db.addImageSet(HARUNA_WINTER);
        db.addImageSet(HARUNA_CLASSIC);
        db.addImageSet(HARUNA_WINTER);
        assertEquals("should be 2 records",2,db.getImageSets().size());
    }




    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        db.close();
    }
}
