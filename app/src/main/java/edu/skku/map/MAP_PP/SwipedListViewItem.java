package edu.skku.map.MAP_PP;

public class SwipedListViewItem {
    private String date;
    private String input;
    private String output;
    private String swipedDate;

    public void listdata(){};

    public String getOutput() {
        return output;
    }

    public String getInput() {
        return input;
    }

    public String getDate() {
        return date;
    }

    public String getSwipedDate() {
        return swipedDate;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSwipedDate(String swipedDate) {
        this.swipedDate = swipedDate;
    }
}