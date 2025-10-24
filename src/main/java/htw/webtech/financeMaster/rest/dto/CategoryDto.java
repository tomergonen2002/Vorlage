package htw.webtech.financeMaster.rest.dto;

public class CategoryDto {
    public Long id;
    public String name;
    public String description;

    public CategoryDto() {}
    public CategoryDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
