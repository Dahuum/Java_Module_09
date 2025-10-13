package fr.school42.sockets.models;

public class Room {
    private Long id;
    private String name;
    private Long ownerId;

    public Room(String name, Long ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }

    public Room(Long id, String name, Long ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }
}