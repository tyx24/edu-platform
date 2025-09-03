import Quill from 'quill';

const BlockEmbed = Quill.import('blots/block/embed');

class VideoBlot extends BlockEmbed {
  static create(value) {
    const node = super.create();
    node.setAttribute('src', value);
    node.setAttribute('controls', true);
    node.setAttribute('style', 'max-width: 100%; height: auto; display: block;');
    return node;
  }

  static value(node) {
    return node.getAttribute('src');
  }
}

VideoBlot.blotName = 'video';
VideoBlot.tagName = 'video';

Quill.register(VideoBlot);
