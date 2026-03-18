import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReportEntry {
    private final StringProperty line;
    private final StringProperty issue;
    private final StringProperty description;

    public ReportEntry(String line, String issue, String description) {
        this.line = new SimpleStringProperty(line);
        this.issue = new SimpleStringProperty(issue);
        this.description = new SimpleStringProperty(description);
    }

    public StringProperty lineProperty() { return line; }
    public StringProperty issueProperty() { return issue; }
    public StringProperty descriptionProperty() { return description; }
}
