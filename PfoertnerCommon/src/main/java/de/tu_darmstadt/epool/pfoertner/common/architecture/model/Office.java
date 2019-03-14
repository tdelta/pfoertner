package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

/**
 * Data model representing an office.
 */
public interface Office {
    /**
     * Every office gets assigned an unique id.
     */
    int getId();

    /**
     * An office can have ann status which is displayed on the main page of the panel.
     *
     * For example: "Do not disturb!"
     */
    String getStatus();

    /**
     * Room number or name of the office.
     */
    String getRoom();

    /**
     * Every office gets assigned a join code by the server. This join code is necessary to join
     * an office and must be supplied to the server when requesting to do so.
     *
     * It is used during the initialization process of the apps and communicated between them
     * using QR codes. See technical documentation of the initialization process.
     */
    String getJoinCode();

    /**
     * URL of the image taken by the front Camera of the door panel, if it took an image.
     */
    String getSpionPicture();

    /**
     * MD5 hash of the image at the URL given by {@link #getSpionPicture()}
     */
    String getSpionPictureMD5();
}
