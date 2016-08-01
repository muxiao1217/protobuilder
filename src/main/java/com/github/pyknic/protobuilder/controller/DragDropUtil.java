package com.github.pyknic.protobuilder.controller;

import com.github.pyknic.protobuilder.project.directory.Handle;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class DragDropUtil {
    
	//***********************************************************
	// 				STATIC
	//***********************************************************
	private static final String HIGHLIGHT_STYLE_CLASS = "drag-drop-highlight";
	
	/**
	 * Adds a method which will be performed when a {@link Node} is dropped
	 * onto another node
	 * 
	 * @param thisNode  			   the node
	 * @param dragDropEventIdentifyer  the name for this drag-drop event
	 * @param action				   the method to perform when a drop is performed
	 */
	public static void add(TreeCell<Handle> thisNode, String dragDropEventIdentifyer, DragDropAction action){			
		//When a drag starts at thisNode
		thisNode.setOnDragDetected( (event) -> {
			Dragboard dragboard = thisNode.startDragAndDrop(TransferMode.MOVE);	//Signals that this node can handle drag events
            
            ClipboardContent clipboardContent = new ClipboardContent();		
            clipboardContent.putString(dragDropEventIdentifyer);
            dragboard.setContent(clipboardContent);
            event.consume();
        } ); 
		
		//When another dragged node hovers over thisNode's space
		thisNode.setOnDragOver( (event) -> {
			Dragboard dragboard = event.getDragboard();			
			if(dragboard.hasString()						   
			&& dragboard.getString().equals( dragDropEventIdentifyer ) ) {
				
				event.acceptTransferModes( TransferMode.MOVE ); 
			}
			event.consume();
		} );
		
		//When another dragged node enters thisNode's space
		thisNode.setOnDragEntered( (event) -> {
			Optional<TreeCell<Handle>> source = getSource(event);
			if( source.isPresent() ){
				Dragboard dragboard = event.getDragboard();
				if(dragboard.hasString()
	            && dragboard.getString().equals(dragDropEventIdentifyer)
	            && !source.get().equals(thisNode) 
	            && !thisNode.getStyleClass().contains(HIGHLIGHT_STYLE_CLASS)) {
					thisNode.getStyleClass().add(HIGHLIGHT_STYLE_CLASS);
				}
			}
			event.consume();
		});
		
		//When another dragged node exits thisNode's space
		thisNode.setOnDragExited( (event) -> {
			Optional<TreeCell<Handle>> source = getSource(event);
			if( source.isPresent() ){
				Dragboard dragboard = event.getDragboard();
				if(dragboard.hasString()
	            && dragboard.getString().equals(dragDropEventIdentifyer)
	            && !source.get().equals(thisNode) 
	            && thisNode.getStyleClass().contains(HIGHLIGHT_STYLE_CLASS)) {
					thisNode.getStyleClass().remove(HIGHLIGHT_STYLE_CLASS);
				}
			}
			event.consume();
		});
		
		//When another dragged node is dropped into thisNode's space
		thisNode.setOnDragDropped( (event) -> {
			Optional<TreeCell<Handle>> source = getSource(event);
			if( source.isPresent() ){
				Dragboard dragboard = event.getDragboard();
				boolean dragDropSucceeded = false;
				if(dragboard.hasString()
	            && dragboard.getString().equals(dragDropEventIdentifyer)
	            && !source.get().equals(thisNode) ) {						
	                action.perform( source.get(), thisNode, getLocation(thisNode, event));
	                dragDropSucceeded = true;
	            } 
	            event.setDropCompleted(dragDropSucceeded);	
			}
            event.consume();
		} );
	}
	
	public enum DropLocation{
		HIGH, LOW, MID
	}
	
    @FunctionalInterface
	public interface DragDropAction{
		/**
		 * The action to perform when a node is dropped onto another node
		 * 
		 * @param dragSource  the object the drag originated from
		 * @param dropTarget  the object the drop was performed on
		 */
		void perform(Node dragSource, Node dropTarget);
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
	private static DropLocation getLocation(TreeCell<?> target, DragEvent event){
		//Get Y pos inside the cell we hover over
		final double yPos = event.getY();
		final double height = target.getHeight();
		if( (height - yPos) / height > 0.75 ) {
			return DropLocation.HIGH;
		} else if ( (height - yPos) / height < 0.25 ) {
			return DropLocation.LOW;
		} else {
			return DropLocation.MID;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Optional<TreeCell<Handle>> getSource(DragEvent event){
		final Object gesture = event.getGestureSource();
		final TreeCell<Handle> sourceNode;
		if( gesture != null && gesture instanceof TreeCell ){
			TreeCell tempCell= (TreeCell) gesture;
			if( tempCell.getItem() instanceof Handle){
				sourceNode = (TreeCell<Handle>) tempCell;
			} else {
				sourceNode = null;
			}
		} else {
			sourceNode = null;
		}
		return Optional.ofNullable(sourceNode);
	}
	
	private DragDropUtil(){
		throw new UnsupportedOperationException("Class " + DragDropUtil.class.getSimpleName() + " should not be instanciated");
	}
}
