// ---------- Animated background (decorative node network) ----------
(function () {
  const bg = document.getElementById('bgFx');
  const bctx = bg.getContext('2d');
  let nodes = [];
  function resize() {
    bg.width = window.innerWidth;
    bg.height = window.innerHeight;
    const count = Math.round((bg.width * bg.height) / 22000);
    nodes = Array.from({ length: count }, () => ({
      x: Math.random() * bg.width,
      y: Math.random() * bg.height,
      vx: (Math.random() - 0.5) * 0.35,
      vy: (Math.random() - 0.5) * 0.35,
    }));
  }
  window.addEventListener('resize', resize);
  resize();
  function frame() {
    bctx.clearRect(0, 0, bg.width, bg.height);
    nodes.forEach(n => {
      n.x += n.vx; n.y += n.vy;
      if (n.x < 0 || n.x > bg.width) n.vx *= -1;
      if (n.y < 0 || n.y > bg.height) n.vy *= -1;
    });
    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        const dx = nodes[i].x - nodes[j].x, dy = nodes[i].y - nodes[j].y;
        const d = Math.hypot(dx, dy);
        if (d < 130) {
          bctx.strokeStyle = `rgba(77,208,225,${(1 - d / 130) * 0.25})`;
          bctx.lineWidth = 0.6;
          bctx.beginPath(); bctx.moveTo(nodes[i].x, nodes[i].y); bctx.lineTo(nodes[j].x, nodes[j].y); bctx.stroke();
        }
      }
    }
    nodes.forEach(n => {
      bctx.fillStyle = 'rgba(77,208,225,0.55)';
      bctx.beginPath(); bctx.arc(n.x, n.y, 1.6, 0, Math.PI * 2); bctx.fill();
    });
    requestAnimationFrame(frame);
  }
  frame();
})();
