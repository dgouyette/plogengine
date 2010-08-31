/*
 * An embeddable textile editor based on Canvas.
 * http://guillaume.bort.fr/textile-editor.html
 *
 * Copyright (c) 2009 Guillaume Bort (http://guillaume.bort.fr) & zenexity (http://www.zenexity.fr)
 * Licensed under the Apache2 license.
 * http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * Date: 2009-08-06
 * Revision: 1
 */
 
var Textile = {};

/** Theme **/
Textile.Theme = {
    
    'PLAIN': {
        background: '#000',
        color: '#FFF'
    },
    
    'SELECTION': {
        background: 'rgba(255, 255, 255, .2)'
    },
    
    'HEADING': {
        background: '#622C10',
        color: '#FEDCC5'
    },
    
    'PARAGRAPH': {
        background: '#100F0B'
    },
    
    'STRONG': {
        color: '#E9C062',
    },
    
    'EM': {
        color: '#E15F9F',
        fontStyle: 'italic'
    },
    
    'CITATION': {
        color: '#8BBBE1',
        fontStyle: 'italic'
    },
    
    'TODO': {
        color: '#F00',
        underline: true
    },
    
    'BLOCKQUOTE': {
        color: '#DED4BA',
        fontStyle: 'italic',
        background: '#100F0B'
    },
    
    'DASH': {
        color: '#DE6112'
    },
    
    'COPYRIGHT': {
         color: '#DE6112',
         fontStyle: 'italic'
    },
    
    'REGISTRED': {
         color: '#DE6112',
         fontStyle: 'italic'
    },
    
    'TRADEMARK': {
         color: '#DE6112',
         fontStyle: 'italic'
    },
    
    'IMAGE': {
        color: '#E7E51A',
        underline: true
    },
    
    'BLOCK_START': {
        color: '#92BCFA'
    },
    
    'ITEM_START': {
        color: '#9C0180'
    },
    
    'BLOCK_STYLE': {
        color: '#92BCFA',
        underline: true
    },
    
    'NOTE_MARK': {
        color: '#E900C0',
        underline: true
    },
    
    'CODE': {
        color: '#7BDE09',
        fontStyle: 'italic',
        background: '#100F0B'
    },
    
    'LINK': {
        color: '#73AC45'
    },
    
    'LINK_URL': {
        color: '#D48C6A',
        underline: true
    }
    
};

/** Some utils **/
Textile.Utils = {
    
    bind: function(func, o) {
        return function() {
            func.apply(o, arguments);
        }
    },
    
    makeClass: function(methods) {
        var fn = function(args) {
            if(!(this instanceof arguments.callee)) {
                return new arguments.callee(arguments);
            }                      
            if( typeof this.constructor == "function") {
                this.constructor.apply(this, args.callee ? args : arguments);
            }                      
        }; 
        fn.prototype = methods; 
        return fn;
    }
    
}

/** The editor itself **/
Textile.Editor = Textile.Utils.makeClass({
    
    model: null,
    ctx: null,
    el: null,
    cursor: null,
    
    lineHeight: 17,
    first_line: 1,
    gutterWidth: 40,
    paddingTop: 5,
    paddingLeft: 5,
    font: '9pt Monaco, Lucida Console, monospace',
    
    hasFocus: false,
    selection: null,
    
    constructor: function(canvasEl) {
        this.el = (typeof(canvasEl) == 'string' ? document.getElementById(canvasEl) : canvasEl);
        if(!this.el.getContext) {
            // Too bad.
            return;
        }
        this.ctx = this.el.getContext('2d');
        if(!this.ctx.fillText) {
            // Too bad.
            return;
        }
        this.model = new Textile.Model(this.el.innerHTML, this);
        this.cursor = new Textile.Cursor(this);
        this.history = new Textile.History(this);
        this.clipboard = new Textile.Clipboard(this);
        
        // Gecko detection
        this.gecko = (document.getBoxObjectFor == undefined) ? false : true;
        
        // Events
        this.el.addEventListener('dblclick', Textile.Utils.bind(this.onDblclick, this), true);
        window.addEventListener('mousedown', Textile.Utils.bind(this.onMousedown, this), true);
        window.addEventListener('mouseup', Textile.Utils.bind(this.onMouseup, this), true);
        window.addEventListener('mousemove', Textile.Utils.bind(this.onMousemove, this), true);
        window.addEventListener('keypress', Textile.Utils.bind(this.onKeypress, this), true);
        window.addEventListener('keydown', Textile.Utils.bind(this.onKeydown, this), true);
        this.el.addEventListener('mousewheel', Textile.Utils.bind(this.onMousewheel, this), true)
        
        // Gecko hacks
        this.el.addEventListener('DOMMouseScroll', Textile.Utils.bind(this.onMousewheelGecko, this), true);
        
        // First
        this.resize(this.el.width, this.el.height);                    
    },
    
    setContent: function(content) {
        this.model.content = content;
        this.model.update();
        this.cursor.bound();
        this.paint();
    },
    
    getContent: function() {
        return this.model.content;
    },
    
    getPosition: function() {
        var pos = $(this.el).position();
        return {
            top: pos.top + parseInt($(this.el).css('borderTopWidth')) + parseInt($(this.el).css('paddingTop')) + parseInt($(this.el).css('marginTop')),
            left: pos.left + parseInt($(this.el).css('borderLeftWidth')) + + parseInt($(this.el).css('paddingLeft')) + + parseInt($(this.el).css('marginLeft'))
        }
    },
    
    resize: function(w, h) {
        this.width = w;
        this.height = h;
        this.el.width = w;
        this.el.height = h;
        this.ctx.font = this.font;
        var txt = ' ';
        for(var i=0; i<500; i++) {
            if(this.ctx.measureText(txt).width < this.width - 10 - this.gutterWidth - 2 * this.paddingLeft) {
                txt += ' ';
            } else {
                this.charWidth = this.ctx.measureText(txt).width / txt.length;
                break;
            }
        }
        this.lineWidth = Math.round((this.width - 10 - this.gutterWidth - 2 * this.paddingLeft ) / this.charWidth);
        this.lines = Math.round((this.height - this.paddingTop * 3) / this.lineHeight);
        this.model.update();
        this.paint();
    },
    
    onMousedown: function(e) {
        if(e.target == this.el) {
            this.hasFocus = true;
        } else {
            this.hasFocus = false;
        }
        // Scrollbar click ?
        if(e.pageX > this.getPosition().left + this.width - 20 && e.target == this.el) {
            var h = this.lines * this.lineHeight;
            var olh = h / this.model.lines.length;
            var bar = this.lines * olh;
            if(bar < 10) bar = 10;
            var o =  (this.first_line - 1) * olh;
            var y = e.pageY - this.getPosition().top - this.paddingTop;
            // The bar itself
            if(y>o && y<o+bar) {
                this.scrollBase = e.pageY;
                this.scrollBaseLine = this.first_line;
            } 
            // Up
            else if (y<o){
                this.onMousewheel({wheelDelta: 1});
            } 
            // Down
            else {
                this.onMousewheel({wheelDelta: -1});
            }
        } 
        // Text click
        else {
            this.selection = null;
            this.bp = true;   
            if(e.target == this.el) {
                this.cursor.fromPointer(this.translate(e));
                this.paint();
            } else {
                this.paint();
            }                     
        }                         
    },
    
    onDblclick: function(e) {
        var txt = this.model.lines[this.cursor.line-1].content;
        var c = this.cursor.column;
        while(txt.charAt(c).match(/\w/) && c > -1) {
            c--;
        }
        c++;
        this.selection = {
            anchor: this.cursor.getPosition(),
            from: c + this.model.lines[this.cursor.line-1].offset,
            to: null
        }
        c = this.cursor.column + 1;
        while(txt.charAt(c).match(/\w/) && c < txt.length) {
            c++;
        }
        this.selection.to = c + this.model.lines[this.cursor.line-1].offset;
        this.paint();
    },
    
    onMouseup: function(e) {
        // Clear all stuff
        this.bp = false;
        this.scrollBase = null;
        clearTimeout(this.autoscroller);
        if(this.selection && (this.selection.from == null || this.selection.to == null)) {
            this.selection = null;
        }
    },
    
    onMousemove: function(e) {
        // Change cursor automatically
        if(e.pageX > this.getPosition().left + this.width - 20 && e.target == this.el) {
            this.el.style.cursor = 'default';
        } else {
            this.el.style.cursor = 'text';
        }
        if(!this.hasFocus) return;
        // A scroll ?
        if(this.scrollBase) {
            var h = this.lines * this.lineHeight;
            var olh = h / this.model.lines.length;
            var line = Math.round((e.pageY - this.scrollBase) / olh) + this.scrollBaseLine;
            this.onMousewheel({}, line);
            return;
        }
        // A selection ?
        if(this.bp) {
            if(!this.selection) {
                this.selection = {
                    anchor: this.cursor.getPosition(),
                    from: null,
                    to: null
                }
            } else {
                this.cursor.fromPointer(this.translate(e));
                var newBound = this.cursor.getPosition();
                if(newBound < this.selection.anchor && this.selection.from != newBound) {
                    this.selection.from = newBound;
                    this.selection.to = this.selection.anchor;
                    this.paint();
                }
                if(newBound > this.selection.anchor && this.selection.to != newBound) {
                    this.selection.from = this.selection.anchor;
                    this.selection.to = newBound;
                    this.paint();
                }                            
                if(newBound == this.selection.anchor && this.selection.from != null) {
                    this.selection.from = null;
                    this.selection.to = null;
                    this.paint();
                }
            }
        }
        // Auto-scroll while selecting
        var auto = false;
        if(this.bp) {
            if(e.pageY < this.getPosition().top) {
                this.onMousewheel({wheelDelta: 1});     
                auto = true;                       
            } 
            if(e.pageY > this.getPosition().top + this.height) {
                this.onMousewheel({wheelDelta: -1});
                auto = true;
            }
        }
        clearTimeout(this.autoscroller);
        if(auto) {
            this.autoscroller = setTimeout(Textile.Utils.bind(function() {
                this.onMousemove(e);
            }, this), 10);
        }                   
    },
    
    onMousewheel: function(e, o) {
        // Hack. Call it with e = null, for direct line access
        if(o != null) {
           this.first_line = o; 
        } else {
            var delta = e.wheelDelta;
            if(delta > 0) {
                this.first_line--;                      
            } else {
                this.first_line++;           
            }
        }
        if(e.preventDefault) e.preventDefault();
        this.cursor.bound();
        this.paint();
    },
    
    onMousewheelGecko: function(e) {
        if(e.axis == e.VERTICAL_AXIS) {
            this.onMousewheel({
                wheelDelta: -e.detail
            });
            e.preventDefault();
        }
    },
    
    onKeypress: function(e) {
        if(!e.charCode || e.charCode == 13 || e.keyCode == 8) {
            if(this.gecko) this.onKeydown(e, true);
            return;
        }
        if(this.hasFocus) {
            this.cursor.show = true;
            var position = this.cursor.getPosition();
            if(e.metaKey || e.ctrlKey) {
                if(e.charCode == 97) {
                    e.preventDefault();
                    this.selection = {
                        anchor: 0,
                        from: 0,
                        to: this.model.content.length
                    }
                    this.paint();
                }
                if(e.charCode == 122) {
                    this.history.undo();
                }
                if(e.charCode == 121) {
                    this.history.redo();
                }
                if(e.charCode == 120) {
                    this.clipboard.cut();
                }
                if(e.charCode == 118) {
                    this.clipboard.paste();
                }
                if(e.charCode == 99) {
                    this.clipboard.copy();
                }
                return;
            }                       
            // CHARS
            var c = String.fromCharCode(e.charCode);
            e.preventDefault();
            if(this.selection) {
                this.model.replace(this.selection.from, this.selection.to, c);
                this.cursor.toPosition(this.selection.from + 1);
                this.selection = null;
            } else {
                this.model.insert(position, c);
                this.cursor.toPosition(position + 1);                         
            }
            this.cursor.focus();
        }
    },
    
    onKeydown: function(e, force) {
        if(this.hasFocus && (!this.gecko || force)) {
            if(e.metaKey || e.ctrlKey) {
                return;
            }
            this.cursor.show = true;
            // ~~~~ MOVE
            if(e.keyCode == 40) {
                e.preventDefault();
                this.cursor.lineDown();
                this.cursor.focus();
                return;
            }
            if(e.keyCode == 38) {
                e.preventDefault();
                this.cursor.lineUp();
                this.cursor.focus();
                return;
            }
            if(e.keyCode == 37) {
                e.preventDefault();
                this.cursor.left();
                this.cursor.focus();
                return;
            }
            if(e.keyCode == 39) {
                e.preventDefault();
                this.cursor.right();
                this.cursor.focus();
                return;
            }   
            // ~~~~ With pos
            var position = this.cursor.getPosition();
            // ENTER
            if(e.keyCode == 13) {
                e.preventDefault();
                if(this.selection) {
                    this.model.replace(this.selection.from, this.selection.to, '\n');
                    this.cursor.toPosition(this.selection.from + 1);
                    this.selection = null;
                } else {
                    this.model.lineBreak(position);
                    this.cursor.toPosition(position+1);                                
                }
                this.cursor.focus();
                return;
            }
            // BACKSPACE
            if(e.keyCode == 8) {
                e.preventDefault();
                if(this.selection) {
                    this.model.replace(this.selection.from, this.selection.to, '');
                    this.cursor.toPosition(this.selection.from);
                    this.selection = null;
                } else {
                    this.model.deleteLeft(position);
                    this.cursor.toPosition(position - 1);
                }
                this.cursor.focus();
                return;
            }
            // TAB
            if(e.keyCode == 9) {
                e.preventDefault();
                if(this.selection) {
                    this.model.replace(this.selection.from, this.selection.to, '    ');
                    this.cursor.toPosition(this.selection.from + 4);
                    this.selection = null;
                } else {
                    this.model.insert(position, '    ');
                    this.cursor.toPosition(position + 4);                            
                }
                this.cursor.focus();
                return;
            }
            // SUPPR 
            if(e.keyCode == 46) {
                e.preventDefault();
                this.model.deleteRight(position);
                this.cursor.toPosition(position);
                this.cursor.focus();
                return;
            }                 
        }
    },
    
    translate: function(e) {
        var pos = this.getPosition();
        return {
            x: e.pageX - pos.left - this.gutterWidth - this.paddingLeft,
            y: e.pageY - pos.top - this.paddingTop
        }
    },
    
    updateCursor: function() {
        this.showCursor = this.hasFocus && !this.showCursor;
        this.paint();
    },
    
    paint: function() {
        this.paintBackground();
        this.paintLineNumbers();
        this.paintSelection();
        this.paintContent();
        this.paintScrollbar();
        this.paintCursor();
    },
    
    paintBackground: function() {
        var style = Textile.Theme['PLAIN'];
        if(style && style.background) {
            this.ctx.fillStyle = style.background;
        } else {
            this.ctx.fillStyle = '#000';            
        }
        this.ctx.fillRect(0, 0, this.width, this.height);
        //
        var parser = new Textile.Parser(this.model, this.first_line, this.first_line + this.lines - 1);
        var token = parser.nextToken();
        var x = 0, y = 1;
        while(token.type != 'EOF') {
            var style = Textile.Theme[token.type];
            if(style && style.background) {
                this.ctx.fillStyle = style.background;
                for(var i=token.startLine-this.first_line; i<=token.endLine-this.first_line; i++) {
                    this.ctx.fillRect(this.gutterWidth + this.paddingLeft, (i) * this.lineHeight + this.paddingTop, this.charWidth * (this.lineWidth-1), this.lineHeight);
                }
            }          
            // Yop
            token = parser.nextToken();
        }
    },
    
    paintSelection: function() {
        if(this.hasFocus) {
            var style = Textile.Theme['SELECTION'];
            if(style && style.background) {
                this.ctx.fillStyle = style.background;
            } else {
                this.ctx.fillStyle = 'rgba(255,255,255,.2)';                
            }
            if(!this.selection) {               
                if(this.cursor.isVisible()) {  
                    this.ctx.fillRect(this.gutterWidth + 1, (this.cursor.line - this.first_line) * this.lineHeight + this.paddingTop, this.width - this.gutterWidth, this.lineHeight);
                }
            } else {
                this.cursor.toPosition(this.selection.from);
                var fl = this.cursor.line, fc = this.cursor.column;
                this.cursor.toPosition(this.selection.to);
                var tl = this.cursor.line, tc = this.cursor.column;
                if(fl == tl) {
                    this.ctx.fillRect(this.gutterWidth + this.paddingLeft + fc * this.charWidth, (fl - this.first_line) * this.lineHeight + this.paddingTop, (tc - fc) * this.charWidth, this.lineHeight);
                } else {
                    for(var i=fl; i<=tl; i++) {
                        if(this.cursor.isLineVisible(i)) {
                            if(i == fl) {
                                this.ctx.fillRect(this.gutterWidth + this.paddingLeft + fc * this.charWidth, (i - this.first_line) * this.lineHeight + this.paddingTop, (this.lineWidth-fc-1) * this.charWidth, this.lineHeight);
                                continue;
                            }
                            if(i == tl) {
                                this.ctx.fillRect(this.gutterWidth + this.paddingLeft, (i - this.first_line) * this.lineHeight + this.paddingTop, tc * this.charWidth, this.lineHeight);
                                continue;
                            }
                            this.ctx.fillRect(this.gutterWidth + this.paddingLeft, (i - this.first_line) * this.lineHeight + this.paddingTop, (this.lineWidth-1) * this.charWidth, this.lineHeight);
                        }
                    }
                }
            }
        }
    },
    
    paintLineNumbers: function() {
        this.ctx.fillStyle = '#DEDEDE';
        this.ctx.fillRect(0, 0, this.gutterWidth, this.height);
        this.ctx.fillStyle = '#8E8E8E';
        this.ctx.fillRect(this.gutterWidth, 0, 1, this.height);
        this.ctx.font = this.font;
        var previousLine = null;
        var rl = 1;
        for(var i=this.first_line; i<this.first_line + this.lines; i++) {
            if(i > this.model.lines.length) {
                break;
            }
            if(this.hasFocus && !this.selection && this.model.lines[i-1].line == this.model.lines[this.cursor.line-1].line) {
                 this.ctx.fillStyle = '#000000'; 
            } else {
                this.ctx.fillStyle = '#888888';                            
            }
            var ln = '';
            if(this.model.lines[i-1].line == previousLine) {
                ln = '\u00B7';
            } else {
                previousLine = (this.model.lines[i-1].line);
                ln = previousLine + '';
            }
            var w = ln.length * 8;
            this.ctx.fillText(ln, this.gutterWidth - this.paddingLeft - w, rl++ * this.lineHeight + this.paddingTop - 4);
        }
    },
    
    paintContent: function() {
        var parser = new Textile.Parser(this.model, this.first_line, this.first_line + this.lines - 1);
        var token = parser.nextToken();
        var x = 0, y = 1;
        while(token.type != 'EOF') {
            if(token.text) {
                var style = Textile.Theme[token.type];        
                if(style && style.color) {
                    this.ctx.fillStyle = style.color;
                } else {
                    this.ctx.fillStyle = '#FFF';                            
                }
                if(style && style.fontStyle) {
                    this.ctx.font = style.fontStyle + ' ' + '12px Monaco, Lucida Console, monospace'; 
                } else {
                    this.ctx.font = '12px Monaco, Lucida Console, monospace';                         
                }
                if(token.text.indexOf('\n') > -1 || token.text.indexOf('\r') > -1) {
                    var lines = token.text.split(/[\n\r]/);
                    for(var i=0; i<lines.length; i++) {
                        if(token.startLine + i >= y + this.first_line - 1 && token.startLine + i <= this.first_line + this.lines - 1) {
                            this.ctx.fillText(lines[i], this.gutterWidth + this.paddingLeft + x * this.charWidth, y * this.lineHeight + this.paddingTop - 4);                        
                            x += lines[i].length;
                            if(i < lines.length - 1 ) {
                                x = 0; y++;
                            }
                        }
                    }
                } else {
                    if(token.startLine >= y + this.first_line - 1 && token.startLine <= this.first_line + this.lines - 1) {      
                        this.ctx.fillText(token.text, this.gutterWidth + this.paddingLeft + x * this.charWidth, y * this.lineHeight + this.paddingTop - 4);                        
                        if(style && style.underline) {
                            this.ctx.fillRect(this.gutterWidth + this.paddingLeft + x * this.charWidth, y * this.lineHeight + this.paddingTop - 4 + 1, token.text.length * this.charWidth + 1, 1);
                        }
                        x += token.text.length;
                    }
                }
            }
            // Yop
            token = parser.nextToken();
        }
    },
    
    paintCursor: function() {
        if(this.hasFocus && this.cursor.show && !this.selection && this.cursor.isVisible()) {
            this.ctx.fillStyle = '#FFF';
            this.ctx.fillRect(this.gutterWidth + this.paddingLeft + this.cursor.column * this.charWidth, this.paddingTop + ((this.cursor.line - this.first_line) * this.lineHeight), 1, this.lineHeight);
        }
    },
    
    paintScrollbar: function() {
        if(this.model.lines.length > this.lines) {
            var h = this.lines * this.lineHeight;
            var olh = h / this.model.lines.length;
            var bar = this.lines * olh;
            var o =  (this.first_line - 1) * olh; 
            // Draw
            this.ctx.strokeStyle = 'rgba(255, 255, 255, .5)';                
            this.ctx.lineWidth = 10;
            this.ctx.beginPath();
            this.ctx.moveTo(this.width - 10, this.paddingTop + o);
            this.ctx.lineTo(this.width - 10, this.paddingTop + o + bar);
            this.ctx.stroke();
        }
    }
    
});

/** Pretty dumb clipboard implementation **/
Textile.Clipboard = Textile.Utils.makeClass({
    
    constructor: function(editor) {
        this.editor = editor;
        this.clipboard = document.createElement('textarea');
        document.body.appendChild(this.clipboard);
        this.clipboard.style.position = 'absolute';
        this.clipboard.style.width = '100px';
        this.clipboard.style.height = '100px';
        this.clipboard.style.top = this.editor.getPosition().top + 'px';
        this.clipboard.style.left = '-999em';
        this.clipboard.autocomplete = 'off';
        this.clipboard.tabIndex = '-1';
    },
    
    cut: function() {
        var data = this.selected();
        if(data) {
            this.copyToClipboard(data);
            this.editor.model.replace(this.editor.selection.from, this.editor.selection.to, '');
            this.editor.cursor.toPosition(this.editor.selection.from);
            this.editor.selection = null;
            this.editor.cursor.focus();
            this.editor.paint();
        }
    },
    
    copy: function() {
        var data = this.selected();
        if(data) {
            this.copyToClipboard(data);
        }                    
    },
    
    paste: function() {
        this.clipboard.select();
        setTimeout(Textile.Utils.bind(function() {
            var data = this.clipboard.value;
            if(data) {
                if(this.editor.selection) {
                    this.editor.model.replace(this.editor.selection.from, this.editor.selection.to, data);
                    this.editor.cursor.toPosition(this.editor.selection.from + data.length);
                    this.editor.selection = null;
                } else {
                    this.editor.model.insert(this.editor.cursor.getPosition(), data);
                    this.editor.cursor.toPosition(this.editor.cursor.getPosition() + data.length);                         
                }
                this.editor.cursor.focus();
                this.editor.paint();    
            }
        }, this), 0);                    
    },
    
    copyToClipboard: function(data) {
        this.clipboard.value = data;
        this.clipboard.select();
    },
    
    selected: function() {
        if(!this.editor.selection) {
            return '';
        }
        return this.editor.model.content.substring(this.editor.selection.from, this.editor.selection.to);
    }
    
});

/** History for undo/redo **/
Textile.History = Textile.Utils.makeClass({
   
   commands: null,
   date: 0,
   
   constructor: function(editor) {
       this.editor = editor;
       this.commands = [];
   },
   
   add: function(command) {
       this.commands = this.commands.slice(0, this.date+1);
       this.commands.push(command);
       this.date = this.commands.length - 1;
   },
   
   undo: function() {
       var lastCommand = this.commands[this.date--];
       if(this.date < -1) this.date = -1;
       if(!lastCommand) return; // no more history
       if(lastCommand.type == 'i') {
           this.editor.model.deleteRight(lastCommand.at, true, lastCommand.txt.length);
           this.editor.cursor.toPosition(lastCommand.cursor);                       
       }
       if(lastCommand.type == 'd') {
           this.editor.model.insert(lastCommand.at, lastCommand.txt, true);
           this.editor.cursor.toPosition(lastCommand.cursor);
       }
       if(lastCommand.type == 'r') {
           this.editor.model.replace(lastCommand.from, lastCommand.from + lastCommand.newTxt.length, lastCommand.oldTxt, true);
           this.editor.cursor.toPosition(lastCommand.from);
           this.editor.selection = lastCommand.selection;
       }
       this.editor.cursor.focus();
       this.editor.paint();
   },
   
   redo: function() {
       var lastCommand = this.commands[++this.date];
       if(this.date > this.commands.length - 1) this.date = this.commands.length - 1;
       if(!lastCommand) return; // no more history
       if(lastCommand.type == 'i') {
           this.editor.model.insert(lastCommand.at, lastCommand.txt, true);
           this.editor.cursor.toPosition(lastCommand.cursor + lastCommand.txt.length);                         
       }
       if(lastCommand.type == 'd') {
           this.editor.model.deleteRight(lastCommand.at, true);
           this.editor.cursor.toPosition(lastCommand.cursor - 1);  
       }
       if(lastCommand.type == 'r') {
           this.editor.model.replace(lastCommand.from, lastCommand.from + lastCommand.oldTxt.length, lastCommand.newTxt, true);
           this.editor.cursor.toPosition(lastCommand.from);
           this.editor.selection = null;  
       }
       this.editor.cursor.focus();
       this.editor.paint();
   }
    
});

/** Cursor **/
Textile.Cursor = Textile.Utils.makeClass({
   
   line: 1,
   column: 0,
   pref_column: 0,
   show: false,
   editor: null,
   
   constructor: function(editor) {
        this.editor = editor;
        setInterval(Textile.Utils.bind(this.toggle, this), 500);
   },
   
   toggle: function() {
        if(this.editor.hasFocus) {
            this.show = !this.show;
            this.editor.paint();
        }
   },
   
   fromPointer: function(position) {
        this.line = Math.round((position.y + (this.editor.lineHeight/2)) / this.editor.lineHeight) + this.editor.first_line - 1;
        this.column = Math.round(position.x / this.editor.charWidth);
        this.pref_column = this.column;
        this.bound();    
        this.show = true;            
   },
   
   isVisible: function() {
        return this.isLineVisible(this.line);
   },
   
   isLineVisible: function(line) {
        return line >= this.editor.first_line && line < this.editor.first_line + this.editor.lines;
   },
   
   focus: function() {
        if(!this.isVisible()) {
            if(this.line < this.editor.first_line) {
                this.editor.first_line = this.line;
            } else {
                this.editor.first_line = this.line - this.editor.lines + 1;
            }
        }
        this.editor.paint();
   },
   
   lineDown: function() {
       if(this.editor.selection) {
           this.toPosition(this.editor.selection.to);
           this.editor.selection = null;
       }
       this.line++;
       if(this.pref_column > this.column) {
           this.column = this.pref_column;
       }
       this.bound();
   },
   
   lineUp: function() {
       if(this.editor.selection) {
           this.toPosition(this.editor.selection.from);
           this.editor.selection = null;
       }
       this.line--;
       if(this.pref_column > this.column) {
           this.column = this.pref_column;
       }
       this.bound();
   },
   
   left: function() {
       if(this.editor.selection) {
           this.toPosition(this.editor.selection.from);
           this.editor.selection = null;
       } else {
           this.toPosition(this.getPosition() - 1);
       }
       this.pref_column = this.column;
   },
   
   right: function(keyboardSelect) {
       if(!keyboardSelect && this.editor.selection) {
           this.toPosition(this.editor.selection.to);
           this.editor.selection = null;
       } else {
           this.toPosition(this.getPosition() + 1);
       }
       this.pref_column = this.column;
   },
   
   bound: function() {
       if(this.line < 1) {
           this.line = 1; 
       }
       if(this.editor.first_line < 1) {
           this.editor.first_line = 1;
       }
       if(this.line > this.editor.model.lines.length) {
           this.line = this.editor.model.lines.length;
       }
       if(this.editor.first_line > this.editor.model.lines.length - this.editor.lines + 1) {
           this.editor.first_line = this.editor.model.lines.length - this.editor.lines + 1;
           if(this.editor.first_line < 1) {
               this.editor.first_line = 1;
           }
       }
       if(this.column < 0) {
           this.column = 0;
       }
       var content = this.editor.model.lines[this.line - 1].content;
       if(this.column > content.length) {
           this.column = content.length;
       }
   },
   
   getPosition: function() {
       return this.editor.model.lines[this.line - 1].offset + this.column;
   },
   
   toPosition: function(position) {
       if(!position || position < 0) {
           position = 0;
       }
       for(var i=0; i<this.editor.model.lines.length; i++) {
           if(this.editor.model.lines[i].offset > position) {
               this.line = i;
               this.column = position - this.editor.model.lines[i-1].offset;
               this.pref_column = this.column;
               if(this.line < 1) {
                   return;
               }
               this.bound();
               return;
           }
       }
       this.line = this.editor.model.lines.length;
       this.column = position - this.editor.model.lines[i-1].offset;
       this.pref_column = this.column;
       if(this.line < 1) {
           return;
       }
       this.bound();
   }
    
});

/** Textile document model **/
Textile.Model = Textile.Utils.makeClass({
   
   content: '',
   lines: [],
   editor: null,
   
   constructor: function(txt, editor) {
       this.content = txt;
       this.editor = editor;
       this.update();
   },
   
   update: function() {
       this.lines = [];
       if(!this.editor.lineWidth) {
           return;
       }
       var offset = 0;
       var lines = this.content.split('\n');
       for(var i=0; i<lines.length; i++) {
           var line = lines[i];
           while(line != null) {
               var part = line.substring(0, this.editor.lineWidth);
               if(part.length == this.editor.lineWidth) {
                   if(part.indexOf(' ') > -1) {
                       part = part.substring(0, part.lastIndexOf(' ') + 1); // Soft wrap on space char
                   } else {
                       part = part.substring(0, part.length); // force
                   }
               }
               this.lines.push({
                  line: i+1,
                  content: part,
                  offset: offset
               });
               offset += part.length;
               if(line.length > part.length) {
                   line = line.substring(part.length);
                   if(!line) line = null;
               } else {
                   line = null;
               }
           }
           offset++; // count linebreak
       }
   },
   
   insert: function(position, txt, noHistory) {
       this.content = this.content.substring(0, position) + txt + this.content.substring(position);
       if(!noHistory) {
           this.editor.history.add({
              type: 'i',
              at: position,
              txt: txt,
              cursor: this.editor.cursor.getPosition() 
           });
       }
       this.update();
   },
   
   replace: function(from, to, txt, noHistory) {
       var deleted = this.content.substring(from, to);
       if(!noHistory) {
           this.editor.history.add({
              type: 'r',
              from: from,
              newTxt: txt,
              oldTxt: deleted,
              selection: this.editor.selection
           });
       }
       this.content = this.content.substring(0, from) + txt + this.content.substring(to);
       this.update();
   },
   
   lineBreak: function(position, noHistory) {
       this.insert(position, '\n');
   },
   
   deleteLeft: function(position, noHistory) {
       var deleted = this.content.substring(position - 1, position);
       if(!noHistory) {
           this.editor.history.add({
               type: 'd',
               at: position - 1,
               txt: deleted,
               cursor: this.editor.cursor.getPosition()
           });
       }
       this.content = this.content.substring(0, position - 1) + this.content.substring(position);                   
       this.update();
   },
   
   deleteRight: function(position, noHistory, size) {
       if(!size) size = 1;
       var deleted = this.content.substring(position, position + size);
       if(!noHistory) {
           this.editor.history.add({
               type: 'd',
               at: position,
               txt: deleted,
               cursor: this.editor.cursor.getPosition()
           });
       }
       this.content = this.content.substring(0, position) + this.content.substring(position + size);
       this.update();
   }
    
});

/** Textile parser **/
Textile.Parser = Textile.Utils.makeClass({
   
   constructor: function(model, from, to) {
       this.model = model;
       this.from = from;
       this.to = to;
       if(!/^$/.test(this.model.lines[this.from-1].content)) {
           while(this.from > 1) {
               if(!/^$/.test(this.model.lines[this.from-1].content)) {
                   this.from--;
               } else {
                   this.from++;
                   break;
               }
           }
       }
       this.text = '';
       for(var i=this.from; i<=this.to; i++) {
           if(i>this.model.lines.length) {
               continue;
           }
           this.text += this.model.lines[i-1].content
           if(this.model.lines[i] && this.model.lines[i].line > this.model.lines[i-1].line) {
               this.text += '\n';
           } else {
               this.text += '\r';
           }                       
       }
       this.len = this.text.length;
       this.end = this.begin = 0;
       this.state = 'PLAIN';
   },
   
   found: function(newState, skip) {
       var begin2 = this.begin;
       var end2 = --this.end + skip;
       this.lastState = this.state;
       var text = this.text.substring(begin2, end2);
       var lines = text.match(/[\n\r]/g);
       var from = this.from;
       var to = this.from + (lines ? lines.length - 1: 0);
       this.from = this.from + (lines ? lines.length: 0);
       this.begin = this.end += skip;
       this.state = newState;
       return {
           type: this.lastState,
           text: text,
           startLine: from,
           endLine: to,
       };
   },
   
   checkHas: function(pattern, skip, noThis) {
       var nc = this.end + (skip ? skip : 0);
       while(nc < this.text.length && this.text.charAt(nc) != '\n') {
           var e = '';
           for(var i=0; i<pattern.length; i++) {
               e += this.text.charAt(nc+i);
           }
           if(e == pattern) {
               return true;
           }
           if(noThis && e.charAt(0).match(noThis)) {
               return false;
           }                       
           nc++;
       }
       return false;
   },
   
   nextToken: function() {     
       for(;;) {
           var left = this.len - this.end;
           if (left < 1 || !left) {
               this.end++;
               return this.found('EOF', 0);
           }

           var c = this.text.charAt(this.end++);
           var c1 = left > 1 ? this.text.charAt(this.end) : 0;
           var c2 = left > 2 ? this.text.charAt(this.end + 1) : 0;
           var c3 = left > 3 ? this.text.charAt(this.end + 2) : 0;

           /** The STATE machine **/
           if(this.state == 'PLAIN') {
                if(c == 'h' && /[1-6]/.test(c1) && c2 == '.') {
                    this.nextBlock = 'HEADING';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'h' && /[1-6]/.test(c1) && c2 == '(' && this.checkHas(').', 3, ' ')) {
                    this.nextBlock = 'HEADING';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'p' && c1 == '.') {
                    this.nextBlock = 'PARAGRAPH';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'p' && c1 == '(' && this.checkHas(').', 2, ' ')) {
                    this.nextBlock = 'PARAGRAPH';
                    return this.found('BLOCK_START', 0);
                }                            
                if(c == 'b' && c1 == 'q' && c2 == '.') {
                    this.nextBlock = 'BLOCKQUOTE';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'b' && c1 == 'q' && c2 == '(' && this.checkHas(').', 2, ' ')) {
                    this.nextBlock = 'BLOCKQUOTE';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'b' && c1 == 'c' && c2 == '.') {
                    this.nextBlock = 'CODE';
                    return this.found('BLOCK_START', 0);
                }
                if(c == 'b' && c1 == 'c' && c2 == '(' && this.checkHas(').', 2, ' ')) {
                    this.nextBlock = 'CODE';
                    return this.found('BLOCK_START', 0);
                }
                if(c == '*' && c1 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c == '*' && c1 == '*' && c2 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c == '*' && c1 == '*' && c2 == '*' && c3 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c == '#' && c1 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c == '#' && c1 == '#' && c2 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c == '#' && c1 == '#' && c2 == '#' && c3 == ' ') {
                       return this.found('ITEM_START', 0);
                }
                if(c != '\n') {
                    return this.found('PARAGRAPH', 0);
                }
           }
           if(this.state == 'BLOCK_START') {
                if(c == '.') {
                    return this.found(this.nextBlock, 1);
                }
                if(c == '(' && this.checkHas(').', 1)) {
                    return this.found('BLOCK_STYLE', 0);
                }
           }
           if(this.state == 'BLOCK_STYLE') {
               if(c == ')' && c1 == '.') {
                   return this.found('BLOCK_START', 1);
               }
           }
           if(this.state == 'ITEM') {
               if(c == '\n' && c1 == '*' && c2 == ' ') {
                   return this.found('ITEM_START', 0);
               }
               if(c == '\n' && c1 == '*' && c2 == '*' && c3 == ' ') {
                   return this.found('ITEM_START', 0);
               }
               if(c == '\n' && c1 == '*' && c2 == '*' && c3 == '*') {
                   return this.found('ITEM_START', 0);
               }
               if(c == '\n' && c1 == '#' && c2 == ' ') {
                   return this.found('ITEM_START', 0);
               }
               if(c == '\n' && c1 == '#' && c2 == '#' && c3 == ' ') {
                   return this.found('ITEM_START', 0);
               }
               if(c == '\n' && c1 == '#' && c2 == '#' && c3 == '#') {
                   return this.found('ITEM_START', 0);
               }
           }
           if(this.state == 'ITEM_START') {
               if(c == ' ') {
                   return this.found('ITEM', 1);
               }
           }
           if(this.state == 'PARAGRAPH' || this.state == 'HEADING' || this.state == 'BLOCKQUOTE' || this.state == 'ITEM') {
                if(c == '*' && c1 == '*' && this.checkHas('**', 2)) {
                    return this.found('STRONG', 0);
                }
                if(c == '_' && c1 == '_' && this.checkHas('__', 2)) {
                    return this.found('EM', 0);
                }
                if(c == '-' && c1 == '-') {
                    return this.found('DASH', 0);
                }
                if(c == '?' && c1 == '?' && this.checkHas('??', 2)) {
                    return this.found('CITATION', 0);
                }
                if(c == '(' && c1 == 'c' && c2 == ')') {
                    return this.found('COPYRIGHT', 0);
                }
                if(c == '(' && c1 == 'r' && c2 == ')') {
                    return this.found('REGISTRED', 0);
                }
                if(c == '(' && c1 == 't' && c2 == 'm' && c3 == ')') {
                    return this.found('TRADEMARK', 0);
                }
                if(c == '[' && c1.match(/\d/) && c2 == ']') {
                    return this.found('NOTE_MARK', 0);
                }
                if(c == '[' && c1.match(/\d/) && c2.match(/\d/) && c3 == ']') {
                    return this.found('NOTE_MARK', 0);
                }
                if(c == '"' && this.checkHas('":', 1, '"')) {
                    return this.found('LINK', 0)
                }
           }
           if(this.state == 'PARAGRAPH' || this.state == 'BLOCKQUOTE') {
               if(c == '!' && c1.match(/\w/) && this.checkHas('!', 1)) {
                   return this.found('IMAGE', 0);
               }
           }
           if(this.state == 'STRONG') {
                if(c != '*' && c1 == '*' && c2 == '*') {
                    return this.found(this.lastState || 'PLAIN', 3);
                }
           }
           if(this.state == 'TODO') {
                return this.found(this.lastState || 'PLAIN', 4);
           }
           if(this.state == 'LINK') {
               if(c == '"' && c1 == ':') {
                   this.statePreviousLink = this.lastState;
                   return this.found('LINK_URL', 2);
               }                           
           }
           if(this.state == 'LINK_URL') {
               if(c.match(/\s/)) {
                   return this.found(this.statePreviousLink || 'PLAIN', 0);
               }
               if(c.match(/[.,;]/) && c1.match(/\s/)) {
                   return this.found(this.statePreviousLink || 'PLAIN', 0);
               }
           }
           if(this.state == 'EM') {
                if(c != '_' && c1 == '_' && c2 == '_') {
                    return this.found(this.lastState || 'PLAIN', 3);
                }
           }
           if(this.state == 'CITATION') {
                if(c != '?' && c1 == '?' && c2 == '?') {
                    return this.found(this.lastState || 'PLAIN', 3);
                }
           }
           if(this.state == 'IMAGE') {
                if(c != '!' && c1 == '!') {
                    return this.found(this.lastState || 'PLAIN', 2);
                }
           }
           if(this.state == 'DASH') {
                return this.found(this.lastState || 'PLAIN', 2);
           }
           if(this.state == 'COPYRIGHT') {
                return this.found(this.lastState || 'PLAIN', 3);
           }
           if(this.state == 'REGISTRED') {
                return this.found(this.lastState || 'PLAIN', 3);
           }
           if(this.state == 'TRADEMARK') {
                return this.found(this.lastState || 'PLAIN', 4);
           }
           if(this.state == 'NOTE_MARK') {
               if(c == ']') {
                   return this.found(this.lastState || 'PLAIN', 1);
               }
           }
           if(true /** MATCH ALL**/) {
               if(c == '\n' && c1 == '\n') {
                   return this.found('PLAIN', 1);
               }
               if(c == 'T' && c1 == 'O' && c2 == 'D' && c3 == 'O') {
                   return this.found('TODO', 0);
               }
           }

       }
   }
    
});
